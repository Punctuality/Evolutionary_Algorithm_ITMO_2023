package additional1.codec;

public record TspProblem(String name, double bestAchievableResult) {
    @Override
    public String toString() {
        return String.format("%s(%d)", name.toUpperCase(), (int) bestAchievableResult);
    }
}
