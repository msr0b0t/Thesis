/*
 * Created by Mary on 19/12/2017.
 */

import org.jgrapht.graph.Multigraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

        //the set of all non empty cores is a list of lists e.g.: {{1, 2, 3}, {0, 2, 4}}
        ArrayList<ArrayList<Integer>> cores = new ArrayList<>();

        ArrayList<String[]> q = new ArrayList<>();

        String[] tempK = new String[numberOfLayers];
        for (int i = 0; i < tempK.length; i++) {
            tempK[i] = "0";
        }

        q.add(tempK);

        // keeps track of the father nodes
        HashMap<String[], ArrayList<String[]>> f = new HashMap<>();
        f.put(tempK, new ArrayList<>());

        while (q.size() > 0) {
            String[] k = q.get(0);
            //dequeue k = [kl] from q
            q.remove(tempK);
            int numberOfNonZeroKl = 0;
            for (String aK : k) {
                if (Integer.parseInt(aK) > 0) {
                    numberOfNonZeroKl++;
                }
            }
            // > or == ?
            if (numberOfNonZeroKl > f.get(k).size()){
                ArrayList<Integer> fIntersection = new ArrayList<>();
                ArrayList<String> tempVString = new ArrayList<>(mg.vertexSet());
                ArrayList<Integer> tempV = new ArrayList<>();
                for (String el : tempVString) {
                    tempV.add(Integer.parseInt(el));
                }
                fIntersection.addAll(kCore(mg, tempV, f.get(k).get(0)));
                for (String[] father : f.get(k)) {
                    Predicate<Integer> filter = (x) -> kCore(mg, tempV, father).indexOf(x) < 0;
                    fIntersection.removeIf(filter);
                }
                ArrayList<Integer> coreDecompositionOfK = kCore(mg, fIntersection, k);
                if (coreDecompositionOfK.size() > 0) {
                    if (cores.indexOf(coreDecompositionOfK) < 0) {
                        cores.add(coreDecompositionOfK);
                    }
                    //enqueue child nodes
                    for (int layer = 0; layer < numberOfLayers; layer++) {
                        String[] kTonos = k;
                        kTonos[layer] = String.valueOf(Integer.parseInt(kTonos[layer]) + 1);

                        //enqueue kTonos into q
                        q.add(kTonos);

                        //correct ?
                        if (!f.get(kTonos).contains(k)){
                            ArrayList<String[]> union = f.get(kTonos);
                            union.add(k);
                            f.remove(kTonos);
                            f.put(kTonos, union);
                        }


                          //typo in paper?

//                        ArrayList<String[]> union = f.get(kTonos);
//
//                        String[] cString = new String[coreDecompositionOfK.size()];
//                        int i = 0;
//                        for (int el : coreDecompositionOfK){
//                            cString[i] = String.valueOf(el);
//                            i++;
//                        }
//                        union.add(cString);
//
//                        HashSet<String[]> hs = new HashSet<>();
//                        hs.addAll(union);
//                        union.clear();
//                        union.addAll(hs);
//
//                        f.put(kTonos, union);
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
