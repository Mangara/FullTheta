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

import fulltheta.data.Pair;
import fulltheta.data.graph.Edge;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LowerboundBacktracker {

//    private static final int MAX_SIZE = 40;
//    private static final double target = 3.5;
//
//    public static void main(String[] args) throws IOException {
//        int k = 7;
//        Pair<Double, Graph> result = computeLowerBound(new Cones(k), 0.01);
//
//        System.out.println("Worst sr for k=" + k + ": " + result.getFirst());
//        save(result.getSecond(), new File("Lowerbound" + k + ".grp"));
//    }
//
//    public static Pair<Double, Graph> computeLowerBound(Cones cones, double position) {
//        Graph graph = new Graph();
//
//        double theta = 2 * Math.PI / cones.getnCones();
//
//        graph.addVertex(new GraphVertex(0, 0)); // origin
//        graph.addVertex(new GraphVertex(position * Math.sin(theta / 2), 1)); // destination
//
//        return backtrackLB(cones, graph, 0.0, "");
//    }
//
//    private static Pair<Double, Graph> backtrackLB(Cones cones, Graph graph, double prevRatio, String spaces) {
//        // Build the spanner
//        SpannerBuilder.buildThetaGraph(graph, cones);
//
//        // Compute the spanning ratio
//        SpanningRatioComputer src = new SpanningRatioComputer(graph);
//        double sr = src.getSpanningRatio();
//
//        System.out.println(String.format("%sBT n=%d, sr=%f, prev=%f", spaces, graph.getVertices().size(), sr, prevRatio));
//
//        if (sr < prevRatio || graph.getVertices().size() > MAX_SIZE || sr >= target) {
//            // The spanning ratio became worse. This isn't going to lead to a good solution.
//            return new Pair<Double, Graph>(sr, graph);
//        }
//
//        // Find the path with the highest spanning ratio and try to remove each of its edges
//        List<Edge> path = src.getMaximalPath();
//        List<Graph> graphs = new ArrayList<Graph>(path.size() * 4);
//
//        for (Edge e : path) {
//            graphs.addAll(removeEdge(cones, graph, e));
//        }
//
//        // Find the modification of this graph with the highest spanning ratio
//        Pair<Double, Graph> opt = new Pair<Double, Graph>(sr, graph);
//
//        for (Graph g : graphs) {
//            Pair<Double, Graph> result = backtrackLB(cones, g, sr, spaces + " ");
//
//            if (result.getFirst() > sr) {
//                opt = result;
//            }
//        }
//
//        return opt;
//    }
//
//    private static List<Graph> removeEdge(Cones cones, Graph graph, Edge edge) {
//        cones.setVertices(graph.getVertices());
//
//        List<GraphVertex> blockAB = new ArrayList<GraphVertex>();
//        List<GraphVertex> blockBA = new ArrayList<GraphVertex>();
//
//        if (cones.getClosestVertexInCone(edge.getVA(), cones.getCone(edge.getVA(), edge.getVB())) == edge.getVB()) {
//            // Get rid of A -> B
//            // Place a vertex in either corner of the canonical triangle, a little closer to A
//            Pair<Pair<Double, Double>, Pair<Double, Double>> canon = computeCanonicalTriangle(edge.getVA(), edge.getVB(), cones);
//
//            blockAB.add(closeTo(canon.getFirst().getFirst(), canon.getFirst().getSecond(), edge.getVA(), edge.getVB()));
//            blockAB.add(closeTo(canon.getSecond().getFirst(), canon.getSecond().getSecond(), edge.getVA(), edge.getVB()));
//        }
//
//        if (cones.getClosestVertexInCone(edge.getVB(), cones.getCone(edge.getVB(), edge.getVA())) == edge.getVA()) {
//            // Get rid of B -> A
//            // Place a vertex in either corner of the canonical triangle, a little closer to B
//            Pair<Pair<Double, Double>, Pair<Double, Double>> canon = computeCanonicalTriangle(edge.getVB(), edge.getVA(), cones);
//
//            blockBA.add(closeTo(canon.getFirst().getFirst(), canon.getFirst().getSecond(), edge.getVB(), edge.getVA()));
//            blockBA.add(closeTo(canon.getSecond().getFirst(), canon.getSecond().getSecond(), edge.getVB(), edge.getVA()));
//        }
//
//        List<Graph> newGraphs = new ArrayList<Graph>();
//
//        if (blockAB.isEmpty()) {
//            for (GraphVertex v : blockBA) {
//                Graph g = new Graph(graph);
//                g.addVertex(v);
//
//                // Check if the new vertex wasn't already there
//                if (g.getVertices().size() == graph.getVertices().size() + 1) {
//                    SpannerBuilder.buildThetaGraph(g, cones);
//
//                    newGraphs.add(g);
//                }
//            }
//        } else if (blockBA.isEmpty()) {
//            for (GraphVertex v : blockAB) {
//                Graph g = new Graph(graph);
//                g.addVertex(v);
//
//                // Check if the new vertex wasn't already there
//                if (g.getVertices().size() == graph.getVertices().size() + 1) {
//                    SpannerBuilder.buildThetaGraph(g, cones);
//
//                    newGraphs.add(g);
//                }
//            }
//        } else {
//            // Both are non-empty: try every combination
//            for (GraphVertex v1 : blockAB) {
//                for (GraphVertex v2 : blockBA) {
//                    Graph g = new Graph(graph);
//                    g.addVertex(v1);
//                    g.addVertex(v2);
//
//                    // Check if the new vertex wasn't already there
//                    if (g.getVertices().size() == graph.getVertices().size() + 2) {
//                        SpannerBuilder.buildThetaGraph(g, cones);
//
//                        newGraphs.add(g);
//                    }
//                }
//            }
//        }
//
//        return newGraphs;
//    }
//
//    private static Pair<Pair<Double, Double>, Pair<Double, Double>> computeCanonicalTriangle(GraphVertex apex, GraphVertex end, Cones cones) {
//        int cone = cones.getCone(apex, end);
//
//        double x1, y1, x2, y2; // Coordinates of the two corners of the canonical triangle
//
//        // Auxilliary variables
//        double theta = 2 * Math.PI / cones.getnCones();
//        double tip = Math.tan(cone * theta + theta / 2);
//        double tim = Math.tan(cone * theta - theta / 2);
//
//        if (cones.getnCones() % 4 == 0 && ((cone == cones.getnCones() / 4) || (cone == 3 * cones.getnCones() / 4))) {
//            // The line perpendicular to the bisector is vertical.
//
//            // The lines along the boundary of cone i have the following formula:
//            //   y = ay + (x - ax) / tan(i * theta +/- theta / 2),
//            // where (ax, ay) is the apex and theta is the angle of a cone.
//            // The bisector has the formula
//            //   y = ay.
//            // So the line perpendicular to the bisector, going through the end point (vx, vy), has formula
//            //   x = vx.
//            // So we can just substitute vx in these formulas to get the coordinates.
//
//            x1 = end.getX();
//            y1 = apex.getY() + tip * (x1 - apex.getX());
//
//            x2 = end.getX();
//            y2 = apex.getY() + tim * (x1 - apex.getX());
//        } else {
//            // The lines along the boundary of cone i have the following formula:
//            //   y = ay + (x - ax) / tan(i * theta +/- theta / 2),
//            // where (ax, ay) is the apex and theta is the angle of a cone.
//            // The bisector has the formula
//            //   y = ay + (x - ax) / tan(i * theta).
//            // So a line perpendicular to the bisector has slope
//            //   -1 / (1 / tan(i * theta)) = -tan(i * theta).
//            // It needs to go through the end point (vx, vy), so it has the formula
//            //   y = vy + tan(i * theta) * (vx - x).
//            // The rest is just computing the intersection points of these lines.
//
//            double ti = Math.tan(cone * theta);
//
//            x1 = (end.getY() - apex.getY() + ti * end.getX() + apex.getX() / tip) / (ti + 1 / tip);
//            y1 = end.getY() + ti * (end.getX() - x1);
//
//            x2 = (end.getY() - apex.getY() + ti * end.getX() + apex.getX() / tim) / (ti + 1 / tim);
//            y2 = end.getY() + ti * (end.getX() - x2);
//        }
//
//        return new Pair<Pair<Double, Double>, Pair<Double, Double>>(new Pair<Double, Double>(x1, y1), new Pair<Double, Double>(x2, y2));
//    }
//
//    private static GraphVertex closeTo(Double x, Double y, GraphVertex apex, GraphVertex end) {
//        double epsilon = 0.00001;
//
//        // Obtain normalized vectors pointing towards the apex and end
//        double vAx = apex.getX() - x;
//        double vAy = apex.getY() - y;
//        double vAlength = Math.sqrt(vAx * vAx + vAy * vAy);
//        vAx /= vAlength;
//        vAy /= vAlength;
//
//        double vBx = end.getX() - x;
//        double vBy = end.getY() - y;
//        double vBlength = Math.sqrt(vBx * vBx + vBy * vBy);
//        vBx /= vBlength;
//        vBy /= vBlength;
//
//        // Move the point epsilon along both vectors
//        return new GraphVertex(x + epsilon * vAx + epsilon * vBx, y + epsilon * vAy + epsilon * vBy);
//    }
//
//    private static void save(Graph graph, File file) throws IOException {
//        BufferedWriter out = null;
//
//        try {
//            out = new BufferedWriter(new FileWriter(file));
//
//            // Write the data
//            out.write(graph.toSaveString());
//        } finally {
//            if (out != null) {
//                out.close();
//            }
//        }
//    }
}
