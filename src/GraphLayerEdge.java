/*
 * Created by Mary on 13/12/2017.
 */

import org.jgrapht.graph.DefaultEdge;


public class GraphLayerEdge<V> extends DefaultEdge {
    private V v1;
    private V v2;
    private String label;

    public GraphLayerEdge(V v1, V v2, String label) {
        this.v1 = v1;
        this.v2 = v2;
        this.label = label;
    }

    public V getV1() {
        return v1;
    }

    public V getV2() {
        return v2;
    }

    public String toString() {
        return label;
    }

}
