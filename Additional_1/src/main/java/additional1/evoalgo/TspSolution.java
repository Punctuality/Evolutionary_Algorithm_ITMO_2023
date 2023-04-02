package additional1.evoalgo;

public class TspSolution {
    public final int[] orderedSolution;

    TspSolution(int[] orderedSolution) {
        this.orderedSolution = orderedSolution;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int idx : orderedSolution) {
            sb.append(idx);
            sb.append('>');
        }
        sb.append(orderedSolution[0]);
        return sb.toString();
    }
}
