package additional1.evoalgo;

import additional1.codec.TspDescription;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.List;

public class TspFitnessFunction implements FitnessEvaluator<TspSolution> {

    final TspDescription problemDescription;

    public TspFitnessFunction(TspDescription problemDescription) {
        this.problemDescription = problemDescription;
    }

    public double getFitness(TspSolution solution, List<? extends TspSolution> list) {
        if (this.problemDescription.dimension != solution.orderedSolution.length)
            throw new IllegalStateException("Solution is not valid for this problem (dimensions are different)");
        if (this.problemDescription.dimension <= 1)
            return 0;
        else {
            double fitness = 0;
            int checker = this.problemDescription.dimension * (this.problemDescription.dimension + 1) / 2;
            for (int i = 1; i <= this.problemDescription.dimension; i++) {
                int fst = solution.orderedSolution[i - 1]; // This way we achieve full route cycle
                int snd = solution.orderedSolution[i == this.problemDescription.dimension ? 0 : i];

                checker -= fst;

                fitness += this.problemDescription.getDistanceBetween(fst, snd);
            }
            if (checker != 0) {
                throw new IllegalStateException("Solution does not contain only unique elems");
            }

            return fitness;
        }

    }

    public boolean isNatural() {
        return false;
    }
}
