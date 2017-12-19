/*
 * Created by mary on 13/12/2017.
 */

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Multigraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class BruteForceAlgorithmLabeledMultigraph {

    public static void main(String[] args) throws IOException {

        //create and print the multigraph
        Multigraph<String, GraphLayerEdge> mg = createMultigraph();
        System.out.println(mg + "\n");

        // find the layers
        List layers = new ArrayList(mg.edgeSet().size());
        int numberOfLayers = 0;

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

        // find the degree of every vertex for all the layers
        int[][] degree = findDegree(mg, numberOfLayers);

        int[] max = new int[numberOfLayers];

        for (int i = 0; i < numberOfLayers; i++) {
            max[i] = 0;
            for (int j = 0; j < mg.vertexSet().size(); j++) {
                if (degree[i][j] > max[i]) {
                    max[i] = degree[i][j];
                }
                System.out.println("Layer: " + (i + 1) + " Vertex: " + j + " Degree: " + degree[i][j]);
            }
            System.out.println("");
        }

        int maxD = 0;
        for (int aMax : max) {
            if (aMax > maxD) {
                maxD = aMax;
            }
        }

        // find the core decomposition for each layer
        ArrayList[][] coreDecomposition = findCoreDecomposition(mg, max, maxD);

        for (int i = 0; i < max.length; i++) {
            for (int j = 0; j <= maxD; j++) {
                System.out.println("Layer: " + (i+1) + " core: " + j + " vertices of the core: " + coreDecomposition[i][j]);
            }
            System.out.println("");
        }

        //find the complete core decomposition
        //findMultilayerCoreDecomposition(coreDecomposition, numberOfLayers, maxD);
    }

    private static Multigraph<String, GraphLayerEdge> createMultigraph() throws IOException {
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

    private static int[][] findDegree(Multigraph<String, GraphLayerEdge> mg, int nol){


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

    private static ArrayList[][] findCoreDecomposition(Multigraph<String, GraphLayerEdge> mg, int[] max,int maxD){

        // c[][] is an array with dimensions the number of layers and the number of cores
        // the value of every element is a list that contains the vertices that are contained in the core
        ArrayList[][] c = new ArrayList[max.length][maxD + 1];

        // make a copy of the graph and find its degree
        Multigraph<String, GraphLayerEdge> tempMg = mg;
        int[][] tempDegree = findDegree(tempMg,max.length);

        //for every layer i compute the core decomposition
        for (int i = 0; i < max.length; i++){
            // for every core vector j
            for (int j = 0; j <= maxD; j++){
                ArrayList<Integer> verticesOfCore = new ArrayList<>();
                // for every vertex k
                for (int k = 0; k < tempMg.vertexSet().size(); k++){
                    if (tempDegree[i][k] == 0){
                        continue;
                    }
                    // if core vector k in layer i is bigger than the degree of the vertex k
                    if (j > tempDegree[i][k]){
                        //update the tempGraph
                        tempMg = updateGraph(tempMg, i, k);
                        //find new degrees
                        tempDegree = findDegree(tempMg,max.length);
                        //clear array list to fill it again
                        verticesOfCore.clear();
                        //check previous vertices for changes in degree
                        k = -1;
                    }else {
                        //add vertex k to the core
                        verticesOfCore.add(k);
                    }
                }
                //System.out.println(verticesOfCore.toString() + "\n");
                c[i][j] = verticesOfCore;
            }
        }

        return c;
    }

    private static Multigraph<String, GraphLayerEdge> updateGraph(Multigraph<String, GraphLayerEdge> g, int layer, int vertex) {

        boolean flag = true;
        while (flag) {
            if (g.edgeSet().size() == 0){
                break;
            }
            for (GraphLayerEdge edge : g.edgeSet()) {
                if (edge.toString().equals(String.valueOf(layer + 1)) && (Objects.equals(String.valueOf(edge.getV1()), String.valueOf(vertex)) || Objects.equals(String.valueOf(edge.getV2()), String.valueOf(vertex)))) {
                    //System.out.printf("\nEdge: {" +edge.getV1() + ", " + edge.getV2() + "} from layer " + (layer+1) + " is removed\n");
                    g.removeEdge(edge);
                    break;
                } else {
                    flag = false;
                }
            }
        }

        return g;
    }

}
