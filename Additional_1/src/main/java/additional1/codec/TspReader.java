package additional1.codec;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import additional1.codec.TspDescription.TspDescriptionBuilder;
import additional1.codec.nodes.*;

public class TspReader {

    static int[][] reading_matrix(Scanner scanner, int dimension) {
        int[][] result = null;
        int i = 0;
        for (
                String curLine = scanner.nextLine();
                !curLine.equalsIgnoreCase("eof");
                curLine = scanner.nextLine(), i++
        ) {
            int[] lineDigits = Arrays.stream(curLine.split("\\s")).mapToInt(Integer::parseInt).toArray();
            if (result == null) {
                result = new int[dimension][dimension];
            }
            result[i] = lineDigits;
        }
        return result;
    }


    static TspNode2D parse2D(String[] elems) {
        int id = Integer.parseInt(elems[0]);
        double[] lineDigits = Arrays.stream(elems).skip(1).mapToDouble(Double::parseDouble).limit(2).toArray();
        return new TspNode2D(id, lineDigits[0], lineDigits[1]);
    }

    static TspNode3D parse3D(String[] elems) {
        int id = Integer.parseInt(elems[0]);
        double[] lineDigits = Arrays.stream(elems).skip(1).mapToDouble(Double::parseDouble).limit(3).toArray();
        return new TspNode3D(id, lineDigits[0], lineDigits[1], lineDigits[2]);
    }

    static TspNodeGeo parseGeoNode(String[] elems) {
        int id = Integer.parseInt(elems[0]);
        double[] lineNums = Arrays.stream(elems).skip(1).mapToDouble(Double::parseDouble).limit(2).toArray();
        return new TspNodeGeo(id, lineNums[0], lineNums[1]);
    }

    static List<TspNode> reading_nodes(Scanner scanner, int dimension, TspEdgeType edgeType) {
        List<TspNode> result = new ArrayList<>(dimension);
        String curLine;
        while(!(curLine = scanner.nextLine()).equalsIgnoreCase("eof")) {

            switch (edgeType) {
                case EUC_2D, MAN_2D, MAX_2D, CEIL_2D -> result.add(parse2D(curLine.split("\\s")));
                case EUC_3D, MAN_3D, MAX_3D -> result.add(parse3D(curLine.split("\\s")));
                case GEO -> result.add(parseGeoNode(curLine.split("\\s")));
                case EXPLICIT, ATT -> {
                    String[] elems = curLine.split("\\s");
                    try {
                        Integer.parseInt(elems[1]);
                        if (elems.length >= 4)
                            result.add(parse3D(elems));
                        else
                            result.add(parse2D(elems));
                    } catch (NumberFormatException ie) {
                        try {
                            Double.parseDouble(elems[1]);
                            result.add(parseGeoNode(elems));
                        } catch (NumberFormatException de) {
                            throw new IllegalArgumentException(String.format("Couldn't parse node coordinates %s", curLine));
                        }
                    }
                }
            }
        }
        return result;
    }

    public static TspDescription readDescription(File file) throws FileNotFoundException {
        TspDescriptionBuilder builder = new TspDescriptionBuilder();
        Scanner scanner = new Scanner(file);

        while(scanner.hasNext()) {
            String curLine = scanner.nextLine();
            String[] valName = curLine.split("\\s*:\\s*");
            switch (valName[0].toLowerCase()) {
                case "name" -> builder.withName(valName[1]);
                case "type" -> builder.withType(valName[1]);
                case "comment" -> builder.withComment(valName[1]);
                case "dimension" -> builder.withDimension(Integer.parseInt(valName[1]));
                case "edge_weight_type" -> builder.withEdgeType(valName[1]);
                case "edge_weight_section" ->
                        builder.withEdgeDistanceMatrix(reading_matrix(scanner, builder.getDimHint()));
                case "node_coord_section" ->
                    reading_nodes(scanner, builder.getDimHint(), builder.getEdgeType()).forEach(builder::withCoord);
                default ->
                        throw new IllegalArgumentException(String.format("Couldn't recognise option: %s", valName[0]));
            }
        }

        scanner.close();

        return builder.result();
    }

}
