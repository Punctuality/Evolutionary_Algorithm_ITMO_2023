package additional1.evoalgo;

import additional1.util.ArrayUtil;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TspMutation implements EvolutionaryOperator<TspSolution> {
    public enum MutationType {
        SWAP(1),
        INSERTION(1),
        SCRAMBLE(1),
        INVERSION(1);

        int maxLen;

        MutationType(int maxLen) {
            this.maxLen = maxLen;
        }

        public int getMaxLen() {
            return this.maxLen;
        }

        public MutationType setMaxLen(int maxLen) {
            this.maxLen = maxLen;
            return this;
        }

        @Override
        public String toString() {
            return String.format("%s(%d)", this.name(), this.maxLen);
        }
    }

    final MutationType type;
    final double mutationProbability;

    public TspMutation(MutationType type, double mutationProbability) {
        this.type = type;
        this.mutationProbability = mutationProbability;
    }

    public TspSolution mutate(TspSolution candidate, Random random) {
        int[] newSolution = candidate.orderedSolution.clone();
        switch (this.type) {
            case SWAP -> {
                int idx1 = random.nextInt(candidate.orderedSolution.length);
                int idx2 = random.nextInt(candidate.orderedSolution.length);
                while (idx1 == idx2) {
                    idx2 = random.nextInt(candidate.orderedSolution.length);
                }

                ArrayUtil.swapElement(newSolution, idx1, idx2);
            }
            case INSERTION -> {
                int idx1 = random.nextInt(candidate.orderedSolution.length - 1);
                int idx2 = random.nextInt(candidate.orderedSolution.length);
                while (idx1 == idx2) {
                    idx2 = random.nextInt(candidate.orderedSolution.length);
                }
                if (idx1 > idx2) {
                    int tmp = idx1;
                    idx1 = idx2;
                    idx2 = tmp;
                }

                newSolution[idx1 + 1] = newSolution[idx2];
                System.arraycopy(candidate.orderedSolution, idx1 + 1, newSolution, idx1 + 2, idx2 - idx1 - 1);
            }
            case SCRAMBLE -> {
                int scrambleSegmentLen = random.nextInt(this.type.getMaxLen()) + 1;
                int scrambleStart = random.nextInt(newSolution.length - scrambleSegmentLen);

                int[] scrambleSection = new int[scrambleSegmentLen];
                System.arraycopy(newSolution, scrambleStart, scrambleSection, 0, scrambleSegmentLen);

                ArrayUtil.scrambleArray(scrambleSection, random);

                System.arraycopy(scrambleSection, 0, newSolution, scrambleStart, scrambleSegmentLen);
            }
            case INVERSION -> {
                int inversionSegmentLen = random.nextInt(this.type.getMaxLen()) + 1;
                int inversionStart = random.nextInt(newSolution.length - inversionSegmentLen);

                int[] insertionSection = new int[inversionSegmentLen];
                System.arraycopy(newSolution, inversionStart, insertionSection, 0, inversionSegmentLen);

                ArrayUtil.reverse(insertionSection);

                System.arraycopy(insertionSection, 0, newSolution, inversionStart, inversionSegmentLen);
            }
        }
        return new TspSolution(newSolution);
    }

    public List<TspSolution> apply(List<TspSolution> population, Random random) {
        ArrayList<TspSolution> results = new ArrayList<>(population.size());

        for (TspSolution candidate : population) {
            if (random.nextDouble() <= this.mutationProbability)
                results.add(mutate(candidate, random));
            else
                results.add(candidate);
        }

        return results;
    }
}
