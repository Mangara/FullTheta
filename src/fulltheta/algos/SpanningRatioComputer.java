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

import fulltheta.data.graph.Edge;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import java.util.ArrayList;
import java.util.List;

public class SpanningRatioComputer {

    private static final int DIRECT_EDGE = -1, NO_PATH = -2;
    private final Graph graph;
    private double spanningRatio;
    private List<Edge> maximalPath;
    private boolean computed = false;

    public SpanningRatioComputer(Graph graph) {
        this.graph = graph;
    }

    public List<Edge> getMaximalPath() {
        if (!computed) {
            computeSpanningRatio();
        }

        return maximalPath;
    }

    public double getSpanningRatio() {
        if (!computed) {
            computeSpanningRatio();
        }

        return spanningRatio;
    }

    public static double computeSpanningRatio(Graph graph) {
        SpanningRatioComputer src = new SpanningRatioComputer(graph);
        return src.getSpanningRatio();
    }

    private void computeSpanningRatio() {
        // Since our graphs are sparse, we could compute all-pairs shortest path 
        // quickly using repeated invocations of Dijkstra's algorithm: O(n^2 + mn)

        // In the meantime, we'll use Floy'd algorithm, which takes O(n^3)
        // Initialization: initialize the shortestPath matrix to be the direct cost matrix
        int n = graph.getVertices().size();

        double[][] shortestPathLength = new double[n][n];
        int[][] path = new int[n][n];

        for (int i = 0; i < n; i++) {
            shortestPathLength[i][i] = 0;

            GraphVertex vi = graph.getVertices().get(i);

            for (int j = i + 1; j < n; j++) {
                Edge e = vi.getEdgeTo(graph.getVertices().get(j));

                if (e == null) {
                    shortestPathLength[i][j] = Double.POSITIVE_INFINITY;
                    path[i][j] = NO_PATH;
                } else {
                    shortestPathLength[i][j] = e.getLength();
                    path[i][j] = DIRECT_EDGE;
                }

                shortestPathLength[j][i] = shortestPathLength[i][j];
                path[j][i] = path[i][j];
            }
        }

        // Main loop
        for (int k = 0; k < n; k++) {
            // For each pair of vertices (i, j) try to use vertex k as intermediate step:
            // If the shortest path i -> k, followed by k -> j is shorter than the current shortest path i -> j, update it.
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (shortestPathLength[i][k] + shortestPathLength[k][j] < shortestPathLength[i][j]) {
                        shortestPathLength[i][j] = shortestPathLength[i][k] + shortestPathLength[k][j];
                        shortestPathLength[j][i] = shortestPathLength[i][j];
                        path[i][j] = k;
                        path[j][i] = path[i][j];
                    }
                }
            }
        }

        // Find the maximum spanning ratio
        spanningRatio = 0;
        int maxSRi = -1;
        int maxSRj = -1;

        for (int i = 0; i < n; i++) {
            GraphVertex vi = graph.getVertices().get(i);

            for (int j = i + 1; j < n; j++) {
                double sr = getSpanningRatio(vi, graph.getVertices().get(j), shortestPathLength[i][j]);

                if (sr > spanningRatio) {
                    spanningRatio = sr;
                    maxSRi = i;
                    maxSRj = j;
                }
            }
        }

        // Find the path corresponding to the maximum spanning ratio
        maximalPath = getPath(path, maxSRi, maxSRj);
        
        computed = true;
    }

    private static double getSpanningRatio(GraphVertex a, GraphVertex b, double shortestPathLength) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return shortestPathLength / Math.sqrt(dx * dx + dy * dy);
    }

    private List<Edge> getPath(int[][] path, int i, int j) {
        if (i == j) {
            // I don't think this will ever happen. Just to be sure...
            return new ArrayList<Edge>();
        } else {
            int k = path[i][j];

            if (k == NO_PATH) {
                return null;
            } else if (k == DIRECT_EDGE) {
                List<Edge> graphPath = new ArrayList<Edge>();
                graphPath.add(graph.getVertices().get(i).getEdgeTo(graph.getVertices().get(j)));
                
                return graphPath;
            } else {
                List<Edge> graphPath = getPath(path, i, k);
                graphPath.addAll(getPath(path, k, j));

                return graphPath;
            }
        }
    }
}
