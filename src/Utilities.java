/*
 * Created by Mary on 18/2/2018.
 */

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Multigraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class Utilities {

    static Multigraph<String, GraphLayerEdge> createMultigraph(String dataset) {
        Multigraph<String, GraphLayerEdge> mg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));
        String line;

        // change the path of the graph
        try {
            FileReader file = new FileReader(dataset);
            BufferedReader br = new BufferedReader(file);
            line = br.readLine();
            String[] parts = line.split("\\s+");
            String v1 = parts[2];
            for (int i = 0; i < Integer.parseInt(v1); i++) mg.addVertex(String.valueOf(i));
            while ((line = br.readLine()) != null) {

                //read each line and set the three strings in three variables
                parts = line.split("\\s+");
                v1 = String.valueOf(Integer.parseInt(parts[1]) - 1);
                String v2 = String.valueOf(Integer.parseInt(parts[2]) - 1);
                String v3 = parts[0];
                mg.addEdge(v1, v2, new GraphLayerEdge<>(v1, v2, v3));

            }
            br.close();
        }catch (IOException e) {
            System.out.println("Couldn't open " + dataset);
            System.exit(-1);
        }
        return mg;
    }

    static Multigraph<String, GraphLayerEdge> updateGraph(Multigraph<String, GraphLayerEdge> g, int layer, int vertex) {

        boolean flag = true;
        while (flag) {
            if (g.edgeSet().size() == 0){
                break;
            }
            for (GraphLayerEdge edge : g.edgeSet()) {
                if (edge.toString().equals(String.valueOf(layer + 1)) && (Objects.equals(String.valueOf(edge.getV1()), String.valueOf(vertex)) || Objects.equals(String.valueOf(edge.getV2()), String.valueOf(vertex)))) {
                    g.removeEdge(edge);
                    flag = true;
                    break;
                } else {
                    flag = false;
                }
            }
        }

        return g;
    }

    static boolean containsStringArray(List<String[]> list, String[] probe) {
        for(String[] element: list){
            if (Arrays.deepEquals(element, probe)){
                return true;
            }
        }
        return false;
    }

    static boolean containsKeyArray(HashMap<String[], ArrayList<String[]>> hm, String[] k) {
        for (String[] key : hm.keySet()){
            if (Arrays.deepEquals(key, k)){
                return true;
            }
        }
        return false;
    }

    static ArrayList<String[]> getValue(HashMap<String[], ArrayList<String[]>> hm, String[] k) {
        for (String[] key : hm.keySet()){
            if (Arrays.deepEquals(key, k)){
                return hm.get(key);
            }
        }
        return new ArrayList<>();
    }

    static HashMap<String[], ArrayList<String[]>> addValue(HashMap<String[], ArrayList<String[]>> hm, String[] k, String[] value) {
        for (String[] key : hm.keySet()) {
            if (Arrays.deepEquals(key, k)) {
                hm.get(key).add(value);
                return hm;
            }
        }
        return hm;
    }

    static boolean containsArraylist(ArrayList<ArrayList<Integer>> big, ArrayList<Integer> small) {
        for(ArrayList<Integer> element: big){
            if (element.equals(small)){
                return true;
            }
        }
        return false;
    }

}
