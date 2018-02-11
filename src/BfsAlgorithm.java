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
        System.out.println(mg + "\n");

        //find the layers
        ArrayList<String> layers = findLayers(mg);

        int numberOfLayers = layers.size();

        // find the complete core decomposition of multigraph
        findCoreDecomposition(mg, numberOfLayers);

    }

    protected static ArrayList<Integer> kCore(Multigraph<String, GraphLayerEdge> mg, ArrayList<Integer> verticesSet, String[] k) {

        ArrayList<Integer> coreDecompositionOfK;

        //layer 1,2...
        ArrayList<String> layers = findLayers(mg);

        //delete all edges from mg of vertices that are not contained in the verticeSet
        for (String v : mg.vertexSet()) {
            if (verticesSet.indexOf(Integer.parseInt(v))< 0) {
                for (int i = 0; i < k.length; i++) {
                    mg = updateGraph(mg, i, Integer.parseInt(v));
                }
            }
        }

        // find the degree of every vertex for all the layers
        int[][] degree = findDegree(mg, layers.size());

        for (int i = 0; i < verticesSet.size(); i++){
            int v = verticesSet.get(i);
            for (Object l : layers) {
                // layer "1" is the layer 0
                int layer = Integer.parseInt(l.toString()) - 1;
                int kl = Integer.parseInt(k[layer]);
                if (degree[layer][v] < kl){
                    // remove vertex v from the set, because it is not contained in the k-core of the graph
                    Iterator itr = verticesSet.iterator();
                    while (itr.hasNext()) {
                        int x = (Integer)itr.next();
                        if (x == v) {
                            itr.remove();
                            i--;
                            break;
                        }
                    }
                }
            }
        }

        coreDecompositionOfK = verticesSet;

        return coreDecompositionOfK;
    }

    protected static void findCoreDecomposition(Multigraph<String, GraphLayerEdge> mg, int numberOfLayers) {

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
            if (numberOfNonZeroKl == f.get(k).size()){

                //{corollary 1}
                //fIntersection is a set of vertices
                ArrayList<Integer> fIntersection = new ArrayList<>();
                if (f.get(k).size() > 0) {
                    String[] tempVString0 = f.get(k).get(0);
                    ArrayList<Integer> tempV0 = new ArrayList<>();
                    for (String aTempVString : tempVString0) {
                        tempV0.add(Integer.parseInt(aTempVString));
                    }
                    fIntersection.addAll(tempV0);
                    for (int i = 1; i < f.get(k).size(); i++) {
                        String[] tempVString = f.get(k).get(i);
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
                ArrayList<Integer> coreDecompositionOfK = kCore(mg, fIntersection, k);

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
                        queue.add(kTonos);

                        //f(k') <- f(k') U {Ck}
                        if (!f.containsKey(kTonos)) {
                            f.put(kTonos, new ArrayList<>());
                        }
                        ArrayList<String[]> union = f.get(kTonos);

                        // turn Ck/ coreDecompositionOfK from int[] into String[]
                        String[] cString = new String[coreDecompositionOfK.size()];
                        int i = 0;
                        for (int el : coreDecompositionOfK){
                            cString[i] = String.valueOf(el);
                            i++;
                        }
                        //add cString to the union
                        union.add(cString);

                        // get all the distinct elements of the union
                        HashSet<String[]> hs = new HashSet<>();
                        hs.addAll(union);
                        union.clear();
                        union.addAll(hs);

                        f.put(kTonos, union);
                    }
                }
            }
        }
        //print the core decomposition of k
        System.out.println("The core decomposition is: " + cores);
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
                    break;
                } else {
                    flag = false;
                }
            }
        }

        return g;
    }

}
