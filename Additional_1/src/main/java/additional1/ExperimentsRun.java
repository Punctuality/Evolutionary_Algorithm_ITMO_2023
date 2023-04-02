package additional1;

import com.google.common.collect.ImmutableList;
import additional1.codec.TspProblem;
import additional1.eval.ExperimentResult;
import additional1.eval.ExperimentUnit;
import additional1.eval.GridEvaluator;
import additional1.evoalgo.TspMutation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

// We need AutoML here :)
class ExperimentsRun {
    final static AtomicInteger experimentCounter = new AtomicInteger();

    final static List<String> paramNames = ImmutableList.of(
            "Problem",
            "Evolution type",
            "Population size",
            "Generations",
            "Mutation probability",
            "Mutation type",
            "Crossover Max len",
            "Crossover probability"
    );

    final static List<TspProblem> problems = ImmutableList.of(
        new TspProblem("wi29", 27603)
    );
    final static List<ExperimentUnit.EvolutionType> evolutionTypes = ImmutableList.of(
            ExperimentUnit.EvolutionType.STEADY_STATE,
            ExperimentUnit.EvolutionType.GENERATIONAL
    );
    final static List<Integer> populationSizes = ImmutableList.of(200);
    final static List<Integer> generations = ImmutableList.of(10000);
    final static List<Double> mutationProbs = ImmutableList.of(0.7, 0.9);
    final static List<TspMutation.MutationType> mutationTypes = ImmutableList.of(
            TspMutation.MutationType.INSERTION
    );
    final static List<Integer> maxLenCrossedOver = ImmutableList.of(5, 10);
    final static List<Double> crossoverProbs = ImmutableList.of(0.01, 0.03);

    final static Function<List<?>, ExperimentUnit> experimentCompiler = objects -> {
    try {
        return new ExperimentUnit(
                experimentCounter,
                (TspProblem) objects.get(0),
                (ExperimentUnit.EvolutionType) objects.get(1),
                (int) objects.get(2),
                (int) objects.get(3),
                (double) objects.get(4),
                (TspMutation.MutationType) objects.get(5),
                (int) objects.get(6),
                (double) objects.get(7),
                5
        );
    } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
    }
};


    final static List<String> resultNames = ImmutableList.of(
            "Best fitness", "Finished eval at", "First generation of best", "Mean time (ms)"
    );
    final static Function<ExperimentResult[], List<String>> rowLogger = results -> {
        ExperimentResult bestOverAll = Arrays
                .stream(results)
                .reduce((a, b) -> a.compareTo(b) < 0 ? a : b)
                .get();

        Double meanTime = ((double) Arrays.stream(results).mapToLong(ExperimentResult::evalTime).sum()) / (double) results.length;

        return ImmutableList.of(
                ((Double) bestOverAll.bestResult()).toString(),
                ((Integer) bestOverAll.finishedEvalAt()).toString(),
                ((Integer) bestOverAll.firstAchievedIter()).toString(),
                meanTime.toString()
        );
    };

    public static void main(String[] args) {
        File outputFile = new File("results/wi29_test.csv");
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        GridEvaluator<ExperimentResult[], ExperimentUnit> evaluator = new GridEvaluator<>(
                outputFile,
                experimentCompiler,
                rowLogger,
                paramNames,
                resultNames,
                problems,
                evolutionTypes,
                populationSizes,
                generations,
                mutationProbs,
                mutationTypes,
                maxLenCrossedOver,
                crossoverProbs
        );

        int processors = Runtime.getRuntime().availableProcessors();
        try (ForkJoinPool executePool = new ForkJoinPool(processors)) {
            System.out.printf("Started executing Grid experiment run on %d processors\n", processors);
            executePool.invoke(evaluator);
            System.out.println("Finished execution of all tasks");
        } catch (Exception e) {
            throw e;
        }
    }
}
