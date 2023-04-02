package additional1.codec.nodes;

public record TspNode3D(int id, double x, double y, double z) implements TspNode {
    @Override
    public int getId() {
        return id;
    }
}
