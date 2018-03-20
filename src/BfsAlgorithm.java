/*
 * Created by Mary on 19/12/2017.
 */

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Multigraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;


public class BfsAlgorithm {

    static Multigraph<String, GraphLayerEdge> mg;
    static ArrayList<String> layers = new ArrayList<>();
    static int numberOfVertices = 0;
    static int[][] degree;

    public static void main(String[] args) throws IOException{

        String graphToUse = "graphs/example.txt";

        // create and print multigraph
        mg = Utilities.createMultigraph(graphToUse);
        numberOfVertices = mg.vertexSet().size();

        //find the layers
        BufferedReader br = new BufferedReader(new FileReader(graphToUse));
        String line = br.readLine();
        int numberOfLayers = Integer.parseInt(line.split("\\s+")[0]);
        for (int i = 1; i < numberOfLayers + 1; i += 1) {
            layers.add(String.valueOf(i));
        }
        br.close();

        degree = new int[layers.size()][numberOfVertices];

        // find the complete core decomposition of multigraph
        findCoreDecomposition();

    }

    private static ArrayList<Integer> kCore(ArrayList<Integer> verticesSet, String[] k) {
        ArrayList<Integer> coreDecompositionOfK;

        //copy mg to tempg
        Multigraph<String, GraphLayerEdge> tempg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));
        org.jgrapht.Graphs.addGraph(tempg, mg);

        // find the degree of every vertex for all the layers
        findDegree();

        //delete all edges from mg of vertices that are not contained in the verticeSet
        for (String v : tempg.vertexSet()) {
            if (!verticesSet.contains(Integer.parseInt(v))) {
                for (int i = 0; i < layers.size(); i++) {
                    updateDegree(tempg, i, Integer.parseInt(v));
                    tempg = Utilities.updateGraph(tempg, i, Integer.parseInt(v));
                }
            }
        }

        for (int i = 0; i < verticesSet.size(); i++){
            int v = verticesSet.get(i);
            for (Object l : layers) {
                // layer "1" is the layer 0
                int layer = Integer.parseInt(l.toString()) - 1;
                int kl = Integer.parseInt(k[layer]);
                if (degree[layer][v] < kl) {
                    // remove vertex v from the set, because it is not contained in the k-core of the graph
                    Iterator itr = verticesSet.iterator();
                    while (itr.hasNext()) {
                        int x = (Integer)itr.next();
                        if (x == v) {
                            itr.remove();
                            // count degrees again
                            updateDegree(tempg, layer, v);
                            // update graph
                            tempg = Utilities.updateGraph(tempg, layer, v);
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

        coreDecompositionOfK = verticesSet;

        return coreDecompositionOfK;
    }

    public static ArrayList<ArrayList<Integer>> findCoreDecomposition() {

        int numberOfLayers = layers.size();

        //the set of all non empty cores is a list of lists e.g.: {{1, 2, 3}, {0, 2, 4}} where every list is a set of vertices
        ArrayList<ArrayList<Integer>> cores = new ArrayList<>();

        //a set of |L|-dimensional vectors k e.g.: {[0, 0, 0], [0, 1, 0]}
        ArrayList<String[]> queue = new ArrayList<>();

        //tempK is the root node [0, 0, 0, ...] as long as the number of layers
        String[] tempK = new String[numberOfLayers];
        for (int i = 0; i < tempK.length; i++) {
            tempK[i] = "0";
        }

        //add the root node to the queue
        queue.add(tempK);

        // keeps track of the father nodes
        HashMap<String[], ArrayList<String[]>> f = new HashMap<>();
        f.put(tempK, new ArrayList<>());

        int numberOfComputedCores = 0;

        while (queue.size() > 0) {
            // get the first element k of the queue
            String[] k = queue.get(0);
            //dequeue k = [kl] from q
            queue.remove(0);
            int numberOfNonZeroKl = 0;
            for (String aK : k) {
                if (Integer.parseInt(aK) > 0) {
                    numberOfNonZeroKl++;
                }
            }

            //{corollary 2}
            if (numberOfNonZeroKl == Utilities.getValue(f, k).size()){

                //{corollary 1}
                //fIntersection is a set of vertices
                ArrayList<Integer> fIntersection = new ArrayList<>();
                if (Utilities.getValue(f, k).size() > 0) {
                    String[] tempVString0 = Utilities.getValue(f, k).get(0);
                    ArrayList<Integer> tempV0 = new ArrayList<>();
                    for (String aTempVString : tempVString0) {
                        tempV0.add(Integer.parseInt(aTempVString));
                    }
                    fIntersection.addAll(tempV0);
                    for (int i = 1; i < Utilities.getValue(f, k).size(); i++) {
                        String[] tempVString = Utilities.getValue(f, k).get(i);
                        ArrayList<Integer> tempV = new ArrayList<>();
                        for (String aTempVString : tempVString) {
                            tempV.add(Integer.parseInt(aTempVString));
                        }
                        Predicate<Integer> filter = (x) -> tempV.indexOf(x) < 0;
                        fIntersection.removeIf(filter);
                    }
                } else {
                    //for the root node we need to get all the vertices of the graph
                    for (String vertex : mg.vertexSet()){
                        fIntersection.add(Integer.parseInt(vertex));
                    }
                }

                //{algorithm 1}
                ArrayList<Integer> coreDecompositionOfK = kCore(fIntersection, k);
                numberOfComputedCores++;
                if (coreDecompositionOfK.size() > 0) {
                    if (cores.indexOf(coreDecompositionOfK) < 0) {
                        cores.add(coreDecompositionOfK);
                    }
                    //enqueue child nodes
                    for (int layer = 0; layer < numberOfLayers; layer++) {
                        //for every layer copy k to kTonos and change it by +1
                        String[] kTonos = k.clone();
                        kTonos[layer] = String.valueOf(Integer.parseInt(kTonos[layer]) + 1);

                        //enqueue kTonos into q
                        if (!Utilities.containsStringArray(queue, kTonos)){
                            queue.add(kTonos);
                        }

                        // if f(kTonos) is empty add a new arraylist
                        if (!Utilities.containsKeyArray(f, kTonos)){
                            f.put(kTonos, new ArrayList<>());
                        }

                        // turn Ck/ coreDecompositionOfK from int[] into String[]
                        String[] cString = new String[coreDecompositionOfK.size()];
                        int i = 0;
                        for (int el : coreDecompositionOfK){
                            cString[i] = String.valueOf(el);
                            i++;
                        }

                        //f(k') <- f(k') U {Ck}
                        f = Utilities.addValue(f, kTonos, cString);
                    }
                }
            }
        }
        // print the core decomposition of k
        System.out.println("The core decomposition is: " + cores);
        // print the number of cores
        System.out.println("Number of computed cores is: " + numberOfComputedCores);
        System.out.println("Number of cores is: " + cores.size());

        return cores;
    }

    private static void findDegree() {

        for (int i = 0; i < layers.size(); i++){
            for (int j = 0; j < numberOfVertices; j++){
                degree[i][j] = 0;
            }
        }

        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = 0; j < numberOfVertices; j++) {
                Set<GraphLayerEdge> setOfEdges = mg.getAllEdges(Integer.toString(i), Integer.toString(j));
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

    private static void updateDegree(Multigraph<String, GraphLayerEdge> tempg, int layer, int vertex) {

        degree[layer][vertex] = 0;

        for (String v : tempg.vertexSet()){
            Set setOfEdges = tempg.getAllEdges(v, Integer.toString(vertex));
            if (setOfEdges == null){
                continue;
            }
            for (Object edge: setOfEdges){
                if (edge.toString().equals(String.valueOf(layer + 1))){
                    degree[layer][Integer.parseInt(v)] --;
                }
            }

        }
    }
}
