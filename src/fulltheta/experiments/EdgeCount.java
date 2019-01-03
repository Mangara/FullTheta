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
package fulltheta.experiments;

import fulltheta.algos.Cones;
import fulltheta.algos.SpannerBuilder;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import java.util.ArrayList;
import java.util.List;

public class EdgeCount {

    public static void main(String[] args) {
        //compareEdges(20);
        //compareEdges(5, 6);
        estimateAverageDegreePlane();
        //test();
    }

    private static void test() {
        int n = 5000;
        Cones cones = new Cones(true, 20);

        Graph graph = new Graph();

        for (int j = 0; j < n; j++) {
            graph.addVertex(new GraphVertex(Math.random(), Math.random()));
        }

        SpannerBuilder.buildConeSpanner(graph, cones);
    }

    private static void compareEdges(int a, int b) {
        Cones conesA = new Cones(true, a);
        Cones conesB = new Cones(true, b);
        int nTrials = 200;

        System.out.printf("%d vs %d\n", a, b);

        for (int n = 10; n < 500; n += 10) {
            double sumEdgesA = 0;
            double sumEdgesB = 0;

            for (int i = 0; i < nTrials; i++) {
                // Build a random Theta graph
                Graph graph = new Graph();

                for (int j = 0; j < n; j++) {
                    graph.addVertex(new GraphVertex(Math.random(), Math.random()));
                }

                SpannerBuilder.buildConeSpanner(graph, conesA);
                sumEdgesA += graph.getEdges().size();

                SpannerBuilder.buildConeSpanner(graph, conesB);
                sumEdgesB += graph.getEdges().size();
            }

            System.out.println(String.format("%d,%f,%f,%f", n, sumEdgesA / nTrials, sumEdgesB / nTrials, (sumEdgesB - sumEdgesA) / sumEdgesA));
        }
    }

    private static void compareEdges(int maxCones) {
        Cones[] cones = new Cones[maxCones];

        for (int i = 2; i < maxCones; i++) {
            cones[i] = new Cones(true, i);
        }

        int nTrials = 200;

        for (int n = 2000; n < 2001; n += 10) {
            double[] edges = new double[maxCones];

            for (int i = 0; i < nTrials; i++) {
                System.out.println(i + "/" + nTrials);
                // Build a random Theta graph
                Graph graph = new Graph();

                System.out.println("Adding vertices.");

                for (int j = 0; j < n; j++) {
                    graph.addVertex(new GraphVertex(Math.random(), Math.random()));
                }

                for (int j = 2; j < maxCones; j++) {
                    System.out.println("Building graph " + j);

                    SpannerBuilder.buildConeSpanner(graph, cones[j]);
                    edges[j] += graph.getEdges().size();
                }
            }

            System.out.print(n);
            for (int i = 2; i < maxCones; i++) {
                System.out.print("," + edges[i] / nTrials);
            }
            System.out.println();
        }
    }
    private static final double CENTERLEFT = 1.0 / 3;
    private static final double CENTERRIGHT = 2.0 / 3;
    private static final double CENTERTOP = 2.0 / 3;
    private static final double CENTERBOTTOM = 1.0 / 3;

    private static void estimateAverageDegreePlane() {
        int maxCones = 21;
        int nTrials = 2000;
        int n = 4000;

        Cones[] cones = new Cones[maxCones];

        for (int i = 2; i < maxCones; i++) {
            cones[i] = new Cones(true, i);
        }

        double[] degree = new double[maxCones];

        for (int i = 0; i < nTrials; i++) {
            System.out.println((i + 1) + "/" + nTrials);
            // Build a random Theta graph
            Graph graph = new Graph();

            // Keep track of the vertices in the center
            List<GraphVertex> centerVertices = new ArrayList<GraphVertex>(n / 9);

            for (int j = 0; j < n; j++) {
                GraphVertex v = new GraphVertex(Math.random(), Math.random());

                if (graph.addVertex(v)) {
                    if (v.getX() > CENTERLEFT && v.getX() < CENTERRIGHT && v.getY() > CENTERBOTTOM && v.getY() < CENTERTOP) {
                        centerVertices.add(v);
                    }
                }
            }

            for (int j = 2; j < maxCones; j++) {
                SpannerBuilder.buildConeSpanner(graph, cones[j]);

                // Compute the average degree of the center vertices
                double totalDegree = 0;

                for (GraphVertex v : centerVertices) {
                    totalDegree += v.getDegree();
                }

                degree[j] += totalDegree / centerVertices.size();
            }
        }

        System.out.print(n);
        for (int i = 2; i < maxCones; i++) {
            System.out.print("," + degree[i] / nTrials);
        }
        System.out.println();
    }
}
