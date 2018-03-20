/*
 * Created by Mary on 15/2/2018.
 */

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Multigraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class DensestSubgraphAlgorithm {

    public static void main(String[] args) throws IOException{

        String graphToUse = "graphs/graph3.txt";

        // create and print multigraph
        BfsAlgorithm.mg = Utilities.createMultigraph(graphToUse);
        BfsAlgorithm.numberOfVertices = BfsAlgorithm.mg.vertexSet().size();

        //find the layers
        BufferedReader br = new BufferedReader(new FileReader(graphToUse));
        String line = br.readLine();
        int numberOfLayers = Integer.parseInt(line.split("\\s+")[0]);
        for (int i = 1; i < numberOfLayers + 1; i += 1) {
            BfsAlgorithm.layers.add(String.valueOf(i));
        }
        br.close();

        BfsAlgorithm.degree = new int[BfsAlgorithm.layers.size()][BfsAlgorithm.numberOfVertices];

        // find the complete core decomposition of multigraph
        ArrayList<ArrayList<Integer>> cores = BfsAlgorithm.findCoreDecomposition();

        // a positive real number (adjustable)
        double b = 2;

        ArrayList<ArrayList<Integer>> lList = new ArrayList<>();
        ArrayList<Integer> allLayers = new ArrayList<>();
        for (int i = 0; i < numberOfLayers; i++) {
            allLayers.add(i+1);
        }
        lList.add(allLayers);

        // add to the list all of the l's
        int i = 0;
        boolean flag = true;
        while(flag) {
            ArrayList<Integer> L = lList.get(i);
            for (int j = 0; j < L.size(); j++) {
                ArrayList<Integer> tempL = new ArrayList<>(L);
                tempL.remove(j);
                if (!Utilities.containsArraylist(lList, tempL) && tempL.size() > 0){
                    lList.add(tempL);
                }
            }
            i++;
            if (!(i < lList.size())){
                flag = false;
            }
        }


        // number of edges
        int noe;

        double result;

        ArrayList<Integer> S = new ArrayList<>();
        double d = 0;
        // compute d
        for (ArrayList<Integer> core : cores) {
            double max = 0;
            double min = 1000;
            for (ArrayList<Integer> L : lList) {
                for (int l : L) {
                    noe = numberOfEdges(l, core);
                    result = (noe * (Math.pow(L.size(), b))) / core.size();
                    if (result < min){
                        min = result;
                    }
                }
                if (min > max){
                    max = min;
                }
            }
            if (max > d){
                d = max;
                S.addAll(core);
            }
        }

        System.out.println("\nGiven b as " + b);
        System.out.println("d = " + d);
        System.out.println("The densest subgraph contains the vertices: " + S);

    }

    private static int numberOfEdges(int l, ArrayList<Integer> core){

        //copy mg to tempg
        Multigraph<String, GraphLayerEdge> tempg = new Multigraph<>(new ClassBasedEdgeFactory<String, GraphLayerEdge>(GraphLayerEdge.class));
        org.jgrapht.Graphs.addGraph(tempg, BfsAlgorithm.mg);

        boolean flag = true;
        while (flag) {
            for (Object vertex : tempg.vertexSet()) {
                if (!core.contains(Integer.parseInt(String.valueOf(vertex)))) {
                    tempg.removeVertex(String.valueOf(vertex));
                    flag = true;
                    break;
                } else {
                    flag = false;
                }
            }
        }

        int numberOfEdges = 0;
        for (GraphLayerEdge edge : tempg.edgeSet()) {
            if (edge.toString().equals(String.valueOf(l))) {
                numberOfEdges++;
            }
        }
        return numberOfEdges;
    }

}
