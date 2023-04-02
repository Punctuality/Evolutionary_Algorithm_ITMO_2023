package additional1.evoalgo;

import additional1.util.ArrayUtil;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import java.util.Random;

public class TspFactory extends AbstractCandidateFactory<TspSolution> {

    final int dimension;

    public TspFactory(int dimension) {
        this.dimension = dimension;
    }


    public TspSolution generateRandomCandidate(Random random) {
        int[] solution = new int[this.dimension];
        for (int i = 0; i < this.dimension; i++) {
            solution[i] = i + 1;
        }

        ArrayUtil.scrambleArray(solution, random);

        return new TspSolution(solution);
    }
}

