package additional1.codec;

import com.google.common.collect.ImmutableList;
import additional1.codec.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class TspDescription {
    public String name;
    public TspType type;
    public List<String> comments;
    public int dimension;
    public TspEdgeType edgeWeightType;
    public TspDistanceMatrix edgeWeightSection;
//    It was this way, but I suppose arrays will be more efficient
//    public LinkedHashMap<Integer, TspNode> nodeCoordSection;
    public TspNode[] nodeCoordSection;

    TspDescription(String name,
                   TspType type,
                   List<String> comments,
                   int dimension,
                   TspEdgeType edgeWeightType,
                   TspDistanceMatrix edgeWeightSection,
                   TspNode[] nodeCoordSection) {
        this.name = name;
        this.type = type;
        this.comments = comments;
        this.dimension = dimension;
        this.edgeWeightType = edgeWeightType;
        this.edgeWeightSection = edgeWeightSection;
        this.nodeCoordSection = nodeCoordSection;
    }

    public static TspDescription blank() {
        return new TspDescription(
                null,
                null,
                null,
                -1,
                null,
                null,
                null
        );
    }

    public double getDistanceBetween(int from, int to) {
        double result;
        switch (this.edgeWeightType) {
            case EUC_2D, CEIL_2D -> {
                result = Math.sqrt(
                        Math.pow(((TspNode2D) this.nodeCoordSection[from - 1]).x() - ((TspNode2D) this.nodeCoordSection[to - 1]).x(), 2) +
                                Math.pow(((TspNode2D) this.nodeCoordSection[from - 1]).y() - ((TspNode2D) this.nodeCoordSection[to - 1]).y(), 2)
                );
                if (this.edgeWeightType == TspEdgeType.CEIL_2D) {
                    result = Math.ceil(result);
                }
            }
            case EUC_3D -> result = Math.sqrt(
                    Math.pow(((TspNode3D) this.nodeCoordSection[from - 1]).x() - ((TspNode3D) this.nodeCoordSection[to - 1]).x(), 2) +
                            Math.pow(((TspNode3D) this.nodeCoordSection[from - 1]).y() - ((TspNode3D) this.nodeCoordSection[to - 1]).y(), 2) +
                            Math.pow(((TspNode3D) this.nodeCoordSection[from - 1]).z() - ((TspNode3D) this.nodeCoordSection[to - 1]).z(), 2)
            );

            case MAN_2D ->
                    result = Math.abs(((TspNode2D) this.nodeCoordSection[from - 1]).x() - ((TspNode2D) this.nodeCoordSection[to - 1]).x()) +
                            Math.abs(((TspNode2D) this.nodeCoordSection[from - 1]).y() - ((TspNode2D) this.nodeCoordSection[to - 1]).y());
            case MAN_3D ->
                    result = Math.abs(((TspNode3D) this.nodeCoordSection[from - 1]).x() - ((TspNode3D) this.nodeCoordSection[to - 1]).x()) +
                            Math.abs(((TspNode3D) this.nodeCoordSection[from - 1]).y() - ((TspNode3D) this.nodeCoordSection[to - 1]).y()) +
                            Math.abs(((TspNode3D) this.nodeCoordSection[from - 1]).z() - ((TspNode3D) this.nodeCoordSection[to - 1]).z());
            case MAX_2D -> result = Math.max(
                    Math.abs(((TspNode2D) this.nodeCoordSection[from - 1]).x() - ((TspNode2D) this.nodeCoordSection[to - 1]).x()),
                    Math.abs(((TspNode2D) this.nodeCoordSection[from - 1]).y() - ((TspNode2D) this.nodeCoordSection[to - 1]).y())
            );
            case MAX_3D -> result = ImmutableList.of(
                    Math.abs(((TspNode3D) this.nodeCoordSection[from - 1]).x() - ((TspNode3D) this.nodeCoordSection[to - 1]).x()),
                    Math.abs(((TspNode3D) this.nodeCoordSection[from - 1]).y() - ((TspNode3D) this.nodeCoordSection[to - 1]).y()),
                    Math.abs(((TspNode3D) this.nodeCoordSection[from - 1]).z() - ((TspNode3D) this.nodeCoordSection[to - 1]).z())
            ).stream().max(Double::compareTo).get();
            case GEO -> {
                double lat1 = ((TspNodeGeo) this.nodeCoordSection[from - 1]).lat();
                double lat2 = ((TspNodeGeo) this.nodeCoordSection[to - 1]).lat();
                double lon1 = ((TspNodeGeo) this.nodeCoordSection[from - 1]).lon();
                double lon2 = ((TspNodeGeo) this.nodeCoordSection[to - 1]).lon();

                final int R = 6371; // Radius of the earth

                double latDistance = Math.toRadians(lat2 - lat1);
                double lonDistance = Math.toRadians(lon2 - lon1);
                double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                result = R * c * 1000;
            }
            case EXPLICIT -> result = this.edgeWeightSection.distanceMatrix()[from][to];
            case ATT -> throw new IllegalStateException("ATT edge type dictates custom distance calc");
            default -> throw new IllegalStateException("Unknown edge type");
        }

        return result;
    }

    public static class TspDescriptionBuilder {
        private final TspDescription intermediate;

        TspDescriptionBuilder() {
            this.intermediate = TspDescription.blank();
        }

        void withName(String name) {
            this.intermediate.name = name;
        }

        void withType(String type) {
            this.intermediate.type = TspType.valueOf(type);
        }

        void withComment(String comment) {
            if (this.intermediate.comments == null)
                this.intermediate.comments = new ArrayList<>(1);
            this.intermediate.comments.add(comment);
        }

        void withDimension(int dimension) {
            this.intermediate.dimension = dimension;
        }

        void withEdgeType(String edgeType) {
            this.intermediate.edgeWeightType = TspEdgeType.valueOf(edgeType);
        }

        void withEdgeDistanceMatrix(int[][] matrix) {
            this.intermediate.edgeWeightSection = new TspDistanceMatrix(matrix);
        }

        void withCoord(TspNode node) {
            if (this.intermediate.nodeCoordSection == null)
                this.intermediate.nodeCoordSection = new TspNode[this.getDimHint()];
            if (this.intermediate.nodeCoordSection[node.getId() - 1] != null)
                throw new IllegalStateException(String.format("Tsp Description already has %d node", node.getId()));
            this.intermediate.nodeCoordSection[node.getId() - 1] = node;
            if (this.intermediate.dimension != -1 && this.intermediate.nodeCoordSection.length > this.intermediate.dimension)
                throw new IllegalStateException(String.format(
                        "Tsp Description coords size exceed set dimension: %d > %d",
                        this.intermediate.nodeCoordSection.length,
                        this.intermediate.dimension
                ));
        }

        int getDimHint() {
            if (this.intermediate.dimension < 0)
                throw new IllegalStateException("Tsp Description has no dimension hint when it's needed");
            return this.intermediate.dimension;
        }

        TspEdgeType getEdgeType() {
            if (this.intermediate.edgeWeightType == null)
                throw new IllegalStateException("Tsp Description has no edge type when it's needed");
            return this.intermediate.edgeWeightType;
        }

        void checkAllByType(Class<? extends TspNode> clazz, String errMessage) {
            if (this.intermediate.nodeCoordSection != null) {
                for (TspNode node : this.intermediate.nodeCoordSection) {
                    if (node.getClass() != clazz)
                        throw new IllegalStateException(errMessage);
                }
            }
        }

        TspDescription result() {
            if (this.intermediate.nodeCoordSection != null) {
                TspNode firstNode = this.intermediate.nodeCoordSection[0];
                checkAllByType(firstNode.getClass(), "Tsp Description has nodes of different types");
            }
            switch (this.intermediate.edgeWeightType) {
                case EUC_2D, MAN_2D, MAX_2D, CEIL_2D ->
                        checkAllByType(TspNode2D.class, "Tsp Description has 2D edge type but nodes are not 2D");
                case EUC_3D, MAN_3D, MAX_3D ->
                        checkAllByType(TspNode3D.class, "Tsp Description has 3D edge type but nodes are not 3D");
                case GEO -> checkAllByType(TspNodeGeo.class, "Tsp Description has GEO edge type but nodes are not GEO");
                case EXPLICIT -> {
                    if (this.intermediate.edgeWeightSection == null)
                        throw new IllegalStateException("Tsp Description has EXPLICIT edge type but no distance matrix");
                }
                case ATT -> {
                }
            }
            return this.intermediate;
        }

    }
}
