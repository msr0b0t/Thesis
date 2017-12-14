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

        //create the multigraph
        Multigraph<String,GraphLayerEdge> mg = createMultigraph();
        System.out.println(mg + "\n");

        List layers = new ArrayList(mg.edgeSet().size());
        int numberOfLayers = 0;

        for (int i = 0; i < mg.vertexSet().size(); i++){
            for (int j = 0; j < mg.vertexSet().size(); j++){
                if (mg.containsEdge(Integer.toString(i), Integer.toString(j))){
                    String label = mg.getEdge(Integer.toString(i), Integer.toString(j)).toString();
                    if (!layers.contains(label)){
                        layers.add(label);
                        numberOfLayers++;
                    }
                }
            }
        }

        int[][] degree = findDegree(mg,numberOfLayers);

        int[] max = new int[numberOfLayers];

        for (int i = 0; i < numberOfLayers; i++){
            max[i] = 0;
            for (int j = 0; j < mg.vertexSet().size(); j++){
                if (degree[i][j] > max[i]){
                    max[i] = degree[i][j];
                }
                System.out.println("Layer: " + (i+1) + " Vertix: " + j + " Degree: " + degree[i][j]);
            }
            System.out.println("");
        }



    }

    private static Multigraph<String, GraphLayerEdge> createMultigraph() throws IOException {
        Multigraph<String, GraphLayerEdge> mg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));

        String line;

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

    private static String[][] findCoreDecomposition(Multigraph<String, GraphLayerEdge> mg, int[][] d, int[] max){

        int maxD = 0;
        for (int i = 0; i < max.length; i++){
            if (max[i] > maxD){
                maxD = max[i];
            }
        }

        String[][] c = new String[maxD][maxD];
        //do stuff
        return c;
    }
}
