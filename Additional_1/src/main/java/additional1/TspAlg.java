package additional1;

import additional1.codec.TspDescription;
import additional1.codec.TspReader;
import additional1.eval.CombinedTerminationCondition;
import additional1.evoalgo.*;
import additional1.graphical.GraphicalEvolutionObserver;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.selection.TournamentSelection;
import org.uncommons.watchmaker.framework.selection.TruncationSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.TargetFitness;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

public class TspAlg {

    public static void main(String[] args) throws Exception {
        String problem = "WI29"; // name of problem or path to input file

        int populationSize = 200; // size of population
        int generations = 50000; // number of generations

        Random random = new Random(45); // random


        File tspFile = new File(String.format("src/main/resources/%s.tsp", problem.toLowerCase()));

        TspDescription description = TspReader.readDescription(tspFile);

        CandidateFactory<TspSolution> factory = new TspFactory(description.dimension); // generation of solutions

        ArrayList<EvolutionaryOperator<TspSolution>> operators = new ArrayList<>();
        operators.add(new TspCrossover(0.05, 15)); // Crossover
        operators.add(new TspMutation(TspMutation.MutationType.SWAP, 0.5)); // Mutation
        operators.add(new TspMutation(TspMutation.MutationType.INSERTION, 0.5)); // Mutation
        operators.add(new TspMutation(TspMutation.MutationType.INVERSION.setMaxLen(10), 0.05)); // Mutation
        operators.add(new TspMutation(TspMutation.MutationType.SCRAMBLE.setMaxLen(20), 0.05)); // Mutation
        EvolutionPipeline<TspSolution> pipeline = new EvolutionPipeline<>(operators);

        SelectionStrategy<Object> selection = new TournamentSelection(new Probability(0.8
        )); // Selection operator

        FitnessEvaluator<TspSolution> evaluator = new TspFitnessFunction(description); // Fitness function

        AbstractEvolutionEngine<TspSolution> algorithm = new GenerationalEvolutionEngine<>(
                factory,
                pipeline,
                evaluator,
                selection,
                random
        );


        algorithm.addEvolutionObserver(populationData -> {
            if (populationData.getGenerationNumber() % 1000 == 0) {
                double bestFit = populationData.getBestCandidateFitness();
                System.out.println("Generation " + populationData.getGenerationNumber() + ": " + bestFit);
                TspSolution best = populationData.getBestCandidate();
                System.out.println("\tBest solution = " + best.toString());
            }
        });

        GraphicalEvolutionObserver graphics = new GraphicalEvolutionObserver(
                800, 800, generations, description
        );

        algorithm.addEvolutionObserver(graphics);

        TerminationCondition terminate = new CombinedTerminationCondition(
                CombinedTerminationCondition.CombineType.OR,
                new GenerationCount(generations + 1),
                new TargetFitness(27749, false)
        );
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        graphics.start();
        long startTime = System.currentTimeMillis();
        algorithm.evolve(populationSize, 1, terminate);
        long endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) + " ms");

        while (graphics.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        graphics.stop();
    }
}
