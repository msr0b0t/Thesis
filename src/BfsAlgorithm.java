/*
 * Created by Mary on 19/12/2017.
 */

import org.jgrapht.graph.Multigraph;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class BfsAlgorithm {

    public static void main(String[] args) throws IOException{
        // create and print multigraph
        Multigraph<String, GraphLayerEdge> mg = BruteForceAlgorithmLabeledMultigraph.createMultigraph();
        System.out.println(mg + "\n");

        //find the layers
        ArrayList<String> layers = BruteForceAlgorithmLabeledMultigraph.findLayers(mg);

        int numberOfLayers = layers.size();

        // find the complete core decomposition of multigraph
        findCoreDecomposition(mg, numberOfLayers);
    }

    protected static ArrayList<Integer> kCore(Multigraph<String, GraphLayerEdge> mg, ArrayList<Integer> verticesSet, String[] k) {

        ArrayList<Integer> coreDecompositionOfK;

        //layer 1,2...
        ArrayList<String> layers = BruteForceAlgorithmLabeledMultigraph.findLayers(mg);

        // find the degree of every vertex for all the layers
        int[][] degree = BruteForceAlgorithmLabeledMultigraph.findDegree(mg, layers.size());

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
            //print the core decomposition of k
            System.out.print("For k = [");
            for (int l = 0; l < k.length - 1; l++){
                System.out.print(k[l] + ", ");
            }
            System.out.println(k[k.length - 1] + "] the core decomposition is: " + cores);
        }
    }

}
