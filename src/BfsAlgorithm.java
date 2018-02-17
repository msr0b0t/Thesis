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

    public static void main(String[] args) throws IOException{
        // create and print multigraph
        Multigraph<String, GraphLayerEdge> mg = createMultigraph();
        //System.out.println(mg + "\n");

        //find the layers
        ArrayList<String> layers = new ArrayList<>(); //findLayers(mg);

        BufferedReader br = new BufferedReader(new FileReader("graphs/homo.txt"));
        String line = br.readLine();
        int numberOfLayers = Integer.parseInt(line.split("\\s+")[0]);
        for (int i = 1; i < numberOfLayers + 1; i += 1) {
            layers.add(String.valueOf(i));
        }
        br.close();

        // find the complete core decomposition of multigraph
        findCoreDecomposition(mg, layers);

    }

    protected static ArrayList<Integer> kCore(Multigraph<String, GraphLayerEdge> mg, ArrayList<Integer> verticesSet, String[] k, ArrayList<String> layers) {

        int nov = mg.vertexSet().size();

        ArrayList<Integer> coreDecompositionOfK;

        //copy mg to tempg
        Multigraph<String, GraphLayerEdge> tempg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));
        org.jgrapht.Graphs.addGraph(tempg, mg);

        // find the degree of every vertex for all the layers
        int[][] degree = findDegree(mg, layers.size(), nov);

        //delete all edges from mg of vertices that are not contained in the verticeSet
        for (String v : tempg.vertexSet()) {
            if (!verticesSet.contains(Integer.parseInt(v))) {
                for (int i = 0; i < layers.size(); i++) {
                    degree = updateDegree(mg, i, Integer.parseInt(v), degree);
                    tempg = updateGraph(tempg, i, Integer.parseInt(v));
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
                            degree = updateDegree(mg, layer, v, degree);
                            // update graph
                            tempg = updateGraph(tempg, layer, v);
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

    protected static void findCoreDecomposition(Multigraph<String, GraphLayerEdge> mg, ArrayList<String> layers) {

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
            if (numberOfNonZeroKl == getValue(f, k).size()){

                //{corollary 1}
                //fIntersection is a set of vertices
                ArrayList<Integer> fIntersection = new ArrayList<>();
                if (getValue(f, k).size() > 0) {
                    String[] tempVString0 = getValue(f, k).get(0);
                    ArrayList<Integer> tempV0 = new ArrayList<>();
                    for (String aTempVString : tempVString0) {
                        tempV0.add(Integer.parseInt(aTempVString));
                    }
                    fIntersection.addAll(tempV0);
                    for (int i = 1; i < getValue(f, k).size(); i++) {
                        String[] tempVString = getValue(f, k).get(i);
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
                ArrayList<Integer> coreDecompositionOfK = kCore(mg, fIntersection, k, layers);
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
                        if (!containsStringArray(queue, kTonos)){
                            queue.add(kTonos);
                        }

                        // if f(kTonos) is empty add a new arraylist
                        if (!containsKeyArray(f, kTonos)){
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
                        f = addValue(f, kTonos, cString);
                    }
                }
            }
        }
        //print the core decomposition of k
        System.out.println("The core decomposition is: " + cores);
        //System.out.println(cores.size());
    }

    protected static Multigraph<String, GraphLayerEdge> createMultigraph() throws IOException {
        Multigraph<String, GraphLayerEdge> mg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));
        String line;

        // change the path of the graph
        BufferedReader br = new BufferedReader(new FileReader("graphs/homo.txt"));
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
        return mg;
    }

    protected static int[][] findDegree(Multigraph<String, GraphLayerEdge> mg, int nol, int nov) {

        int[][] d = new int[nol][nov];
        for (int i = 0; i < nol; i++){
            for (int j = 0; j < nov; j++){
                d[i][j] = 0;
            }
        }

        for (int i = 0; i < nov; i++) {
            for (int j = 0; j < nov; j++) {
                Set<GraphLayerEdge> setOfEdges = mg.getAllEdges(Integer.toString(i), Integer.toString(j));
                if (setOfEdges == null) {
                    continue;
                }
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

    protected static int[][] updateDegree(Multigraph<String,GraphLayerEdge> mg, int layer, int vertex, int[][] d) {

        d[layer][vertex] = 0;

        for (String v : mg.vertexSet()){
            Set setOfEdges = mg.getAllEdges(v, Integer.toString(vertex));
            if (setOfEdges == null){
                continue;
            }
            for (Object edge: setOfEdges){
                if (edge.toString().equals(String.valueOf(layer + 1))){
                    d[layer][Integer.parseInt(v)] --;
                }
            }

        }
        return d;
    }

    protected static boolean containsStringArray(List<String[]> list, String[] probe) {
        for(String[] element: list){
            if (Arrays.deepEquals(element, probe)){
                return true;
            }
        }
        return false;
    }

    protected static boolean containsKeyArray(HashMap<String[], ArrayList<String[]>> hm, String[] k) {
        for (String[] key : hm.keySet()){
            if (Arrays.deepEquals(key, k)){
                return true;
            }
        }
        return false;
    }

    protected static ArrayList<String[]> getValue(HashMap<String[], ArrayList<String[]>> hm, String[] k) {
        for (String[] key : hm.keySet()){
            if (Arrays.deepEquals(key, k)){
                return hm.get(key);
            }
        }
        ArrayList<String[]> values = new ArrayList<>();
        return values;
    }

    protected static HashMap<String[], ArrayList<String[]>> addValue(HashMap<String[], ArrayList<String[]>> hm, String[] k, String[] value) {
        for (String[] key : hm.keySet()) {
            if (Arrays.deepEquals(key, k)) {
                hm.get(key).add(value);
                return hm;
            }
        }
        return hm;
    }

}
