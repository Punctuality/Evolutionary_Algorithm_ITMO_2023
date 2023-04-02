package additional1.eval;

import additional1.codec.*;
import additional1.evoalgo.*;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.TargetFitness;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ExperimentUnit extends RecursiveTask<ExperimentResult[]> {


    public enum EvolutionType {
        STEADY_STATE,
        GENERATIONAL
    }

    final AtomicInteger experimentCounter;

//    General setup
    final TspProblem problem;
    final TspDescription problemDescription;

    final EvolutionType evolutionType; // Connected vars
    final int populationSize;

    final int generations;
    final Random[] randoms;
//    Mutation
    final double mutationProbability;

    final TspMutation.MutationType mutationType; // Connected vars
//    Crossover
    final double crossOverProbability;
    final int maxLenCrossedOver;

    public ExperimentUnit(
            AtomicInteger experimentCounter,
            TspProblem problem,
            EvolutionType evolutionType,
            int populationSize,
            int generations,
            double mutationProbability,
            TspMutation.MutationType mutationType,
            int maxLenCrossedOver,
            double crossOverProbability,
            int repeatCount
            ) throws FileNotFoundException {
        this.experimentCounter = experimentCounter;
        this.problemDescription = TspReader.readDescription(new File(
                String.format("src/main/resources/%s.tsp", problem.name().toLowerCase())
        ));
        this.problem = problem;
        this.evolutionType = evolutionType;
        this.populationSize = populationSize;
        this.generations = generations;
        this.randoms = new Random[repeatCount];
        for (int i = 0; i < repeatCount; i++) {
            this.randoms[i] = new Random(i);
        }
        this.mutationProbability = mutationProbability;
        this.mutationType = mutationType;
        this.maxLenCrossedOver = maxLenCrossedOver;
        this.crossOverProbability = crossOverProbability;
    }

    RecursiveTask<ExperimentResult> setUpTask(
            CandidateFactory<TspSolution> factory,
            EvolutionPipeline<TspSolution> pipeline,
            FitnessEvaluator<TspSolution> evaluator,
            SelectionStrategy<Object> selection,
            Random random
    ) {

        final AbstractEvolutionEngine<TspSolution> algorithm = switch (this.evolutionType) {
            case STEADY_STATE -> new SteadyStateEvolutionEngine<>(
                        factory,
                        pipeline,
                        evaluator,
                        selection,
                        this.populationSize,
                        false,
                        random
                );
            case GENERATIONAL -> new GenerationalEvolutionEngine<>(
                        factory,
                        pipeline,
                        evaluator,
                        selection,
                        random
                );
        };

        algorithm.setSingleThreaded(true);

        AtomicReference<Double> bestFit = new AtomicReference<>(Double.MAX_VALUE);
        AtomicInteger firstAchievedIter = new AtomicInteger(0);
        AtomicInteger lastGeneration = new AtomicInteger(0);

        algorithm.addEvolutionObserver(populationData -> {
            double currentFit = populationData.getBestCandidateFitness();
            lastGeneration.set(populationData.getGenerationNumber());
            if (bestFit.get() > currentFit) {
                bestFit.set(currentFit);
                firstAchievedIter.set(populationData.getGenerationNumber());
            }
        });

        TerminationCondition terminate = new CombinedTerminationCondition(CombinedTerminationCondition.CombineType.OR,
                new GenerationCount(generations),
                new TargetFitness(this.problem.bestAchievableResult() + 1e-2, false)
        );

        return new RecursiveTask<>() {
            @Override
            protected ExperimentResult compute() {
                long startTime = System.currentTimeMillis();
                TspSolution bestCandidate = algorithm.evolve(populationSize, 1, terminate);
                long endTime = System.currentTimeMillis();

                return new ExperimentResult(
                        bestFit.get(),
                        bestCandidate,
                        firstAchievedIter.get(),
                        lastGeneration.get(),
                        endTime - startTime
                );
            }
        };
    }


    @Override
    protected ExperimentResult[] compute() {
        FitnessEvaluator<TspSolution> evaluator = new TspFitnessFunction(problemDescription);

        CandidateFactory<TspSolution> factory = new TspFactory(problemDescription.dimension);

        ArrayList<EvolutionaryOperator<TspSolution>> operators = new ArrayList<>();
        operators.add(new TspCrossover(this.crossOverProbability, this.maxLenCrossedOver));
        operators.add(new TspMutation(this.mutationType, this.mutationProbability));
        EvolutionPipeline<TspSolution> pipeline = new EvolutionPipeline<>(operators);

        SelectionStrategy<Object> selection = new RouletteWheelSelection();

        ArrayList<RecursiveTask<ExperimentResult>> tasks = new ArrayList<>(this.randoms.length);

        for (Random random : this.randoms) {
            tasks.add(setUpTask(factory, pipeline, evaluator, selection, random));
        }

        int experimentId = this.experimentCounter.incrementAndGet();

        System.out.printf(
                "Running experiment #%d [problem: %s, populationSize: %d, generations: %d, evoType: %s, mutType: %s(%d), mutationProb: %.3f, crossOverLen: %d, crossOverProb: %.3f]\n",
                experimentId,
                this.problem.name(),
                this.populationSize,
                this.generations,
                this.evolutionType.name(),
                this.mutationType.name(),
                this.mutationType.getMaxLen(),
                this.mutationProbability,
                this.maxLenCrossedOver,
                this.crossOverProbability
        );

        var results = ForkJoinTask.invokeAll(tasks).stream().map(ForkJoinTask::join).toArray(ExperimentResult[]::new);

        System.out.printf("Finished experiment #%d\n", experimentId);

        return results;
    }
}
