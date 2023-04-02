package additional1.codec.nodes;

public record TspNodeGeo(int id, double lat, double lon) implements TspNode {
    @Override
    public int getId() {
        return id;
    }
}
