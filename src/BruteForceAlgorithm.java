/*
 * Created by mary on 13/12/2017.
 */

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Multigraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;


public class BruteForceAlgorithm {

    public static void main(String[] args) throws IOException {

        //create and print the multigraph
        Multigraph<String, GraphLayerEdge> mg = createMultigraph();
        System.out.println(mg + "\n");

        // find the layers
        ArrayList layers = findLayers(mg);
        int numberOfLayers = layers.size();

        // find the degree of every vertex for all the layers
        int[][] degree = findDegree(mg, numberOfLayers);

        int[] max = new int[numberOfLayers];

        for (int i = 0; i < numberOfLayers; i++) {
            max[i] = 0;
            for (int j = 0; j < mg.vertexSet().size(); j++) {
                if (degree[i][j] > max[i]) {
                    max[i] = degree[i][j];
                }
            }
        }

        int maxD = 0;
        for (int aMax : max) {
            if (aMax > maxD) {
                maxD = aMax;
            }
        }

        //find the complete core decomposition
        findCoreDecomposition(mg, numberOfLayers, maxD);
    }

    protected static Multigraph<String, GraphLayerEdge> createMultigraph() throws IOException {
        Multigraph<String, GraphLayerEdge> mg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));

        String line;

        // change the path of the graph
        BufferedReader br = new BufferedReader(new FileReader("graphs/graph3.txt"));
        while ((line = br.readLine()) != null) {

            //read each line and set the three strings in three variables
            String[] parts = line.split("\\s+");
            String v1 = parts[0];
            String v2 = parts[1];
            String v3 = parts[2];

            mg.addVertex(v1);
            mg.addVertex(v2);

            mg.addEdge(v1, v2, new GraphLayerEdge<>(v1, v2, v3));

        }
        return mg;
    }

    protected static ArrayList findLayers(Multigraph<String, GraphLayerEdge> mg) {

        int numberOfLayers = 0;

        ArrayList<String> layers = new ArrayList(mg.edgeSet().size());

        for (int i = 0; i < mg.vertexSet().size(); i++) {
            for (int j = 0; j < mg.vertexSet().size(); j++) {
                if (mg.containsEdge(Integer.toString(i), Integer.toString(j))) {
                    String label = mg.getEdge(Integer.toString(i), Integer.toString(j)).toString();
                    if (!layers.contains(label)) {
                        layers.add(label);
                        numberOfLayers++;
                    }
                }
            }
        }
        return layers;
    }

    protected static int[][] findDegree(Multigraph<String, GraphLayerEdge> mg, int nol){

        int[][] d = new int[nol][mg.vertexSet().size()];

        for (int i = 0; i < mg.vertexSet().size(); i++) {
            for (int j = 0; j < mg.vertexSet().size(); j++) {
                Set setOfEdges = mg.getAllEdges(Integer.toString(i), Integer.toString(j));
                for (int k = 0; k < setOfEdges.size(); k++) {
                    int l = Integer.parseInt(setOfEdges.toArray()[k].toString());
                    d[l - 1][i]++;
                }
            }
        }

        return d;
    }

    protected static Multigraph<String, GraphLayerEdge> updateGraph(Multigraph<String, GraphLayerEdge> g, int layer, int vertex) {

        boolean flag = true;
        while (flag) {
            if (g.edgeSet().size() == 0){
                break;
            }
            for (GraphLayerEdge edge : g.edgeSet()) {
                if (edge.toString().equals(String.valueOf(layer + 1)) && (Objects.equals(String.valueOf(edge.getV1()), String.valueOf(vertex)) || Objects.equals(String.valueOf(edge.getV2()), String.valueOf(vertex)))) {
                    //System.out.printf("\nEdge: {" +edge.getV1() + ", " + edge.getV2() + "} from layer " + (layer+1) + " is removed\n");
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

    protected static void findCoreDecomposition(Multigraph<String, GraphLayerEdge> mg, int nol, int nov){

        // create all_Ks array
        // all_Ks[i] is e.g.: ['4','5','0']
        ArrayList<String[]> all_Ks = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(new String(new char[nol]).replace("\0", String.valueOf(nov))); i++) {
            boolean allCool = true;
            String[] tmp = String.valueOf(i).split("(?!^)");
            ArrayList<String> tmpArrList = new ArrayList<>(Arrays.asList(tmp));
            while (tmpArrList.size() < nol) tmpArrList.add(0, "0");
            for (String aTmpArrList : tmpArrList) {
                if (Integer.parseInt(aTmpArrList) > nov) {
                    allCool = false;
                    break;
                }
            }
            if (allCool) all_Ks.add(tmpArrList.toArray(new String[tmpArrList.size()]));
        }

        ArrayList<ArrayList<Integer>> coreDecompositionOfK = new ArrayList<>();

        // make a copy of the graph and find its degree
        Multigraph<String, GraphLayerEdge> tempMg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));
        org.jgrapht.Graphs.addGraph(tempMg, mg);

        // find the degree of every vertex for all the layers
        int[][] degree;


        for (String[] k : all_Ks) {
            org.jgrapht.Graphs.addGraph(tempMg, mg);
            ArrayList<Integer> verticesSet = new ArrayList<>();
            for (String v : tempMg.vertexSet()){
                verticesSet.add(Integer.parseInt(v));
            }
            degree = findDegree(tempMg, nol);
            for (int i = 0; i < verticesSet.size(); i++){
                int v = verticesSet.get(i);
                for (int l = 0; l < nol; l++) {
                    int kl = Integer.parseInt(k[l]);
                    if (degree[l][v] < kl) {
                        // remove vertex v from the set, because it is not contained in the k-core of the graph
                        Iterator itr = verticesSet.iterator();
                        while (itr.hasNext()) {
                            int x = (Integer)itr.next();
                            if (x == v) {
                                itr.remove();
                                for (int layer = 0; layer < nol; layer++) {
                                    tempMg = updateGraph(tempMg, layer, v);
                                }
                                // count degrees again
                                degree = findDegree(tempMg, nol);
                                //go to the beginning
                                i = 0;
                                break;
                            }
                        }
                    }
                }
            }
            // the set cannot contain only one vertex
            if (verticesSet.size() == 1){
                verticesSet.clear();
            }
            //System.out.println(verticesSet);
            if (!coreDecompositionOfK.contains(verticesSet) && !(verticesSet.size() == 0)) {
                coreDecompositionOfK.add(verticesSet);
            }
        }

        for (ArrayList list : coreDecompositionOfK){
            System.out.println(list);
        }
    }

}
