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
import fulltheta.algos.SpanningRatioComputer;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;

public class RandomTheta {

    public static void main(String[] args) {
        runRandomTheta();
    }

    private static void runRandomTheta() {
        int nCones = 5;
        Cones cones = new Cones(true, nCones);
        int nTrials = 200;
        
        System.out.println("Theta " + nCones);
        
        for (int n = 10; n < 500; n += 10) {
            double maxSR = 0;
            double sumSR = 0;

            for (int i = 0; i < nTrials; i++) {
                // Build a random Theta graph
                Graph graph = new Graph();

                for (int j = 0; j < n; j++) {
                    graph.addVertex(new GraphVertex(Math.random(), Math.random()));
                }

                SpannerBuilder.buildConeSpanner(graph, cones);
                
                double sr = SpanningRatioComputer.computeSpanningRatio(graph);
                
                if (sr > maxSR) {
                    maxSR = sr;
                }
                
                sumSR += sr;
            }
            
            System.out.println(String.format("%d,%f,%f",n,sumSR / nTrials,maxSR));
        }
    }
}
