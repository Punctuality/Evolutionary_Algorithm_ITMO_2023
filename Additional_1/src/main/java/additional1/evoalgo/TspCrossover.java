package additional1.evoalgo;

import com.google.common.collect.ImmutableList;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TspCrossover extends AbstractCrossover<TspSolution> {

    final int maxLenSwapped;

    public TspCrossover(double crossOverProbability, int maxLenSwapped) {
        super(1, new Probability(crossOverProbability));
        this.maxLenSwapped = maxLenSwapped;
    }

    int[] orderCrossover(int[] from, int[] to, Random random) {
        int[] toNew = to.clone();

        int swapSegmentLen = random.nextInt(maxLenSwapped) + 1;
        int swapPoint = random.nextInt(from.length - swapSegmentLen);
        int[] swapSegment = new int[swapSegmentLen];
        System.arraycopy(from, swapPoint, swapSegment, 0, swapSegmentLen);

        System.arraycopy(swapSegment, 0, toNew, swapPoint, swapSegmentLen);

        Arrays.sort(swapSegment);

        boolean jumped = false;
        for (int i = 0, j = 0; i < to.length; i++) {
            if (j >= swapPoint && !jumped) {
                j += swapSegmentLen;
                jumped = true;
            }
            if (Arrays.binarySearch(swapSegment, to[i]) < 0) {
                toNew[j++] = to[i];
            }
        }

        return toNew;
    }

    protected List<TspSolution> mate(TspSolution p1, TspSolution p2, int numberOfCrossoverPoints, Random random) {
        if (p1.orderedSolution.length != p2.orderedSolution.length)
            throw new IllegalStateException("Input candidates cannot have different dimensions on crossover");


        int[] p1New = p1.orderedSolution;
        int[] p2New = p2.orderedSolution;
        for (int i = 0; i < numberOfCrossoverPoints; i++) {
            int[] tmp = orderCrossover(p1New, p2New, random);
            p1New = orderCrossover(p2New, p1New, random);
            p2New = tmp;
        }

        return ImmutableList.of(new TspSolution(p1New), new TspSolution(p2New));
    }
}
