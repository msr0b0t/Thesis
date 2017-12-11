/*
 * Created by Mary on 01/12/2017.
 * This is a brute force algorithm that reads a graph file and finds the core decomposition of the graph
 */

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class BruteForceAlgorithm {

    public static void main(String[] args) throws IOException {

        Graph<String, DefaultEdge> initialGraph = createGraph();
        System.out.println(initialGraph);

        ArrayList degree;

        String[] coreD;

        degree = findDegree(initialGraph);
        System.out.println(degree);

        coreD = findCoreDecomposition(degree, initialGraph);
        for (int i = 0; i < coreD.length; i++) {
            System.out.println(coreD[i]);
        }

    }


    //Create a graph based on an input file.
    private static Graph<String, DefaultEdge> createGraph() throws IOException {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        String line;

        BufferedReader br = new BufferedReader(new FileReader("graphs/graph1.txt"));
        while ((line = br.readLine()) != null) {

            //read each line and set the two nodes in two variables
            String[] parts = line.split("\\s+");
            String v1 = parts[0];
            String v2 = parts[1];

            //check if the edge (v1,v2) already exists in graph g
            //and if not, then add it to graph g
            if (!(g.containsEdge(v1, v2) || g.containsEdge(v2, v1))) {
                g.addVertex(v1);
                g.addVertex(v2);
                g.addEdge(v1, v2);
            }

        }

        return g;
    }


    private static ArrayList findDegree(Graph<String, DefaultEdge> g) {

        ArrayList d = new ArrayList<>();
        int degree;

        //Set of graph's vertices
        Set vertexHashSet = g.vertexSet();

        //we transfer the hash set to an array list to get it sorted
        List sortedList = new ArrayList(vertexHashSet);
        Collections.sort(sortedList, new Comparator<String>() {
            //we use this function to have the numbers sorted correctly (not with an order like 0,1,11,12,...)
            public int compare(String o1, String o2) {
                Integer i1 = Integer.parseInt(o1);
                Integer i2 = Integer.parseInt(o2);
                return (i1 < i2 ? -1 : (Objects.equals(i1, i2) ? 0 : 1));
            }
        });

        //for each node, find and store the degree in an array list
        for (Object s : sortedList) {
            degree = (g.edgesOf(s.toString()).size());
            d.add(degree);
        }
        return d;
    }


    private static String[] findCoreDecomposition(ArrayList d, Graph<String, DefaultEdge> g) throws IOException{
        //coreness vector k can be up to the maximum degree of graph
        int max = (Integer) Collections.max(d, null);

        // function returns a string array that contains the degrees of the vertices of every k-core
        String[] c = new String[max + 1];

        //i is the vector of the core
        for (int i = 0; i <= max; i++) {
            //initialising c[]
            c[i] ="";
            // j is the vertex degree counter
            for (int j = 0; j < d.size(); j++) {
                // vertex j has been removed along with the edges
                if ((Integer)d.get(j) == 0) {
                    c[i] += "0";
                    continue;
                }
                // vertex j will not be contained at the core
                if (i > (Integer) d.get(j)) {
                    //update the graph and find the new degrees
                    g = updateGraph(g, Integer.toString(j), max);
                    d = findDegree(g);
                    c[i] += "0";
                    j = -1;
                    // return to the j = 0 to check again
                }else {
                    // vertex j will be contained at the core
                    c[i] += d.get(j).toString();
                }
            }
        }
        return c;
    }


    private static Graph<String, DefaultEdge> updateGraph(Graph<String, DefaultEdge> g, String v, int size) throws IOException {
        for (int i = 0; i < size; i++){
            g.removeEdge(v, Integer.toString(i));
            g.removeEdge(Integer.toString(i), v);

       }
        return g;
    }

}