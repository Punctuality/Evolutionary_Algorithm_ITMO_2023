package additional1.codec.nodes;

public record TspNode2D(int id, double x, double y) implements TspNode {
    @Override
    public int getId() {
        return id;
    }
}
