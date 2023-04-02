package additional1.codec.nodes;

sealed public interface TspNode permits TspNode2D, TspNode3D, TspNodeGeo {
    int getId();
}
