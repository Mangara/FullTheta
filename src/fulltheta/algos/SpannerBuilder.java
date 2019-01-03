/*
 * The MIT License
 *
 * Copyright 2019 Sander Verdonschot.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fulltheta.algos;

import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import java.util.List;

public class SpannerBuilder {

    /**
     * Removes all edges from the specified graph and replaces them by the edges of our spanner.
     * @param graph
     */
    public static void buildConeSpanner(Graph graph, Cones cones, boolean directed) {
        // Remove all edges
        graph.clearEdges();
        
        // Preprocess vertices
        cones.setVertices(graph.getVertices());

        for (GraphVertex v : graph.getVertices()) {
            List<GraphVertex> closest = cones.getClosestVertices(v);
            
            for (GraphVertex u : closest) {
                graph.addEdge(v, u, directed);
            }
        }
    }
    
    public static void buildConeSpanner(Graph graph, Cones cones) {
        buildConeSpanner(graph, cones, false);
    }
}
