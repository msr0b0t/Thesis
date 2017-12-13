/*
 * Created by mary on 13/12/2017.
 */

import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Multigraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class BruteForceAlgorithmLabeledMultigraph {

    public static void main(String[] args) throws IOException {

        //create the multigraph
        Graph<String,GraphLayerEdge> mg = createMultigraph();
        System.out.println(mg);


        //check if number of edges is correct
//        for (int i = 0; i <= 5; i++){
//            for (int j = 0; j <= 5; j++){
//                if (mg.containsEdge(Integer.toString(i), Integer.toString(j))){
//                    System.out.println(i + " " + j + " " + mg.getEdge(Integer.toString(i), Integer.toString(j)).getLabel());
//                }
//            }
//        }
//
//        System.out.println(mg.degreeOf(""));


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

    private static ArrayList findDegree (Multigraph<String, GraphLayerEdge> mg){
        ArrayList d = new ArrayList<>();

        //compute the degrees of the multigraph

        return d;
    }
}
