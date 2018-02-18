/*
 * Created by Mary on 13/12/2017.
 */

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Multigraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BruteForceAlgorithm {

    private static Multigraph<String, GraphLayerEdge> mg;
    private static ArrayList<String> layers = new ArrayList<>();
    private static int numberOfVertices = 0;
    private static int[][] degree;

    public static void main(String[] args) throws IOException {

        //create the multigraph
        mg = Utilities.createMultigraph("graphs/test.txt");
        numberOfVertices = mg.vertexSet().size();

        //find the layers
        BufferedReader br = new BufferedReader(new FileReader("graphs/test.txt"));
        String line = br.readLine();
        int numberOfLayers = Integer.parseInt(line.split("\\s+")[0]);
        for (int i = 1; i < numberOfLayers + 1; i += 1) {
            layers.add(String.valueOf(i));
        }
        br.close();

        // find the degree of every vertex for all the layers
        degree = new int[layers.size()][numberOfVertices];
        findDegree(mg);

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

    private static void findDegree(Multigraph<String, GraphLayerEdge> tempG) {

        for (int i = 0; i < layers.size(); i++){
            for (int j = 0; j < numberOfVertices; j++){
                degree[i][j] = 0;
            }
        }

        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = 0; j < numberOfVertices; j++) {
                Set<GraphLayerEdge> setOfEdges = tempG.getAllEdges(Integer.toString(i), Integer.toString(j));
                if (setOfEdges == null) {
                    continue;
                }
                for (int k = 0; k < setOfEdges.size(); k++) {
                    int l = Integer.parseInt(setOfEdges.toArray()[k].toString());
                    degree[l - 1][i]++;
                }
            }
        }

    }

    private static void findCoreDecomposition(Multigraph<String, GraphLayerEdge> mg, int nol, int nov){

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

        for (String[] k : all_Ks) {
            org.jgrapht.Graphs.addGraph(tempMg, mg);
            ArrayList<Integer> verticesSet = new ArrayList<>();
            for (String v : tempMg.vertexSet()){
                verticesSet.add(Integer.parseInt(v));
            }
            findDegree(tempMg);
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
                                    tempMg = Utilities.updateGraph(tempMg, layer, v);
                                }
                                // count degrees again
                                findDegree(tempMg);
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
