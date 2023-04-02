package additional1.eval;

import additional1.evoalgo.TspSolution;

import java.util.Comparator;
import java.util.Objects;

public record ExperimentResult(
        double bestResult,
        TspSolution bestCandidate,
        int firstAchievedIter,
        int finishedEvalAt,
        long evalTime
) implements Comparable<ExperimentResult> {

    @Override
    public int compareTo(ExperimentResult that) {
        Objects.requireNonNull(that);
        return Objects.compare(this, that,
                Comparator.comparingDouble(ExperimentResult::bestResult)
                        .thenComparingDouble(ExperimentResult::finishedEvalAt)
                        .thenComparingDouble(ExperimentResult::firstAchievedIter)
                        .thenComparingDouble(ExperimentResult::evalTime)
        );
    }
}
