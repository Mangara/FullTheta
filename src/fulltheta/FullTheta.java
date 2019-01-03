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
package fulltheta;

import fulltheta.algos.Cones;
import fulltheta.algos.SpannerBuilder;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import fulltheta.gui.MainFrame;
import fulltheta.ipe.IPEExporter;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class FullTheta {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        MainFrame mf = new MainFrame();
        mf.setVisible(true);
        mf.showConesDialog();
    }

    public static void generateRandomSpanners() throws IOException {
        int nGraphs = 100;
        int nVertices = 100;
        int nCones = 6;
        boolean theta = true;

        Random rand = new Random();
        Cones cones = new Cones(theta, nCones);
        IPEExporter exp = new IPEExporter();

        for (int i = 0; i < nGraphs; i++) {
            Graph graph = new Graph();

            for (int j = 0; j < nVertices; j++) {
                graph.addVertex(new GraphVertex(rand.nextDouble(), rand.nextDouble()));
            }

            SpannerBuilder.buildConeSpanner(graph, cones);

            exp.exportGraph(new File("forsale/tSpanner_" + i + ".ipe"), graph, false);
        }
    }
}
