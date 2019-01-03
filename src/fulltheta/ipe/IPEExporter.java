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
package fulltheta.ipe;

import fulltheta.data.embedded.EmbeddedGraph;
import fulltheta.data.embedded.EmbeddedVertex;
import fulltheta.data.embedded.HalfEdge;
import fulltheta.data.graph.Constraint;
import fulltheta.data.graph.Edge;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import java.awt.geom.Line2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class IPEExporter {

    private static final String IPE7HEADER =
            // XML stuff
            "<?xml version=\"1.0\"?>\n"
            + "<!DOCTYPE ipe SYSTEM \"ipe.dtd\">\n"
            + // We require IPE version 7
            "<ipe version=\"70010\" creator=\"RectangularCartogram\">\n"
            + // Basic IPE style
            "<ipestyle name=\"basic\">\n"
            + "<symbol name=\"arrow/arc(spx)\">\n"
            + "<path stroke=\"sym-stroke\" fill=\"sym-stroke\" pen=\"sym-pen\">\n"
            + "0 0 m\n"
            + "-1 0.333 l\n"
            + "-1 -0.333 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"arrow/farc(spx)\">\n"
            + "<path stroke=\"sym-stroke\" fill=\"white\" pen=\"sym-pen\">\n"
            + "0 0 m\n"
            + "-1 0.333 l\n"
            + "-1 -0.333 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"mark/circle(sx)\" transformations=\"translations\">\n"
            + "<path fill=\"sym-stroke\">\n"
            + "0.6 0 0 0.6 0 0 e\n"
            + "0.4 0 0 0.4 0 0 e\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"mark/disk(sx)\" transformations=\"translations\">\n"
            + "<path fill=\"sym-stroke\">\n"
            + "0.6 0 0 0.6 0 0 e\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"mark/fdisk(sfx)\" transformations=\"translations\">\n"
            + "<group>\n"
            + "<path fill=\"sym-stroke\" fillrule=\"eofill\">\n"
            + "0.6 0 0 0.6 0 0 e\n"
            + "0.4 0 0 0.4 0 0 e\n"
            + "</path>\n"
            + "<path fill=\"sym-fill\">\n"
            + "0.4 0 0 0.4 0 0 e\n"
            + "</path>\n"
            + "</group>\n"
            + "</symbol>\n"
            + "<symbol name=\"mark/box(sx)\" transformations=\"translations\">\n"
            + "<path fill=\"sym-stroke\" fillrule=\"eofill\">\n"
            + "-0.6 -0.6 m\n"
            + "0.6 -0.6 l\n"
            + "0.6 0.6 l\n"
            + "-0.6 0.6 l\n"
            + "h\n"
            + "-0.4 -0.4 m\n"
            + "0.4 -0.4 l\n"
            + "0.4 0.4 l\n"
            + "-0.4 0.4 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"mark/square(sx)\" transformations=\"translations\">\n"
            + "<path fill=\"sym-stroke\">\n"
            + "-0.6 -0.6 m\n"
            + "0.6 -0.6 l\n"
            + "0.6 0.6 l\n"
            + "-0.6 0.6 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"mark/fsquare(sfx)\" transformations=\"translations\">\n"
            + "<group>\n"
            + "<path fill=\"sym-stroke\" fillrule=\"eofill\">\n"
            + "-0.6 -0.6 m\n"
            + "0.6 -0.6 l\n"
            + "0.6 0.6 l\n"
            + "-0.6 0.6 l\n"
            + "h\n"
            + "-0.4 -0.4 m\n"
            + "0.4 -0.4 l\n"
            + "0.4 0.4 l\n"
            + "-0.4 0.4 l\n"
            + "h\n"
            + "</path>\n"
            + "<path fill=\"sym-fill\">\n"
            + "-0.4 -0.4 m\n"
            + "0.4 -0.4 l\n"
            + "0.4 0.4 l\n"
            + "-0.4 0.4 l\n"
            + "h\n"
            + "</path>\n"
            + "</group>\n"
            + "</symbol>\n"
            + "<symbol name=\"mark/cross(sx)\" transformations=\"translations\">\n"
            + "<group>\n"
            + "<path fill=\"sym-stroke\">\n"
            + "-0.43 -0.57 m\n"
            + "0.57 0.43 l\n"
            + "0.43 0.57 l\n"
            + "-0.57 -0.43 l\n"
            + "h\n"
            + "</path>\n"
            + "<path fill=\"sym-stroke\">\n"
            + "-0.43 0.57 m\n"
            + "0.57 -0.43 l\n"
            + "0.43 -0.57 l\n"
            + "-0.57 0.43 l\n"
            + "h\n"
            + "</path>\n"
            + "</group>\n"
            + "</symbol>\n"
            + "<symbol name=\"arrow/fnormal(spx)\">\n"
            + "<path stroke=\"sym-stroke\" fill=\"white\" pen=\"sym-pen\">\n"
            + "0 0 m\n"
            + "-1 0.333 l\n"
            + "-1 -0.333 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"arrow/pointed(spx)\">\n"
            + "<path stroke=\"sym-stroke\" fill=\"sym-stroke\" pen=\"sym-pen\">\n"
            + "0 0 m\n"
            + "-1 0.333 l\n"
            + "-0.8 0 l\n"
            + "-1 -0.333 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"arrow/fpointed(spx)\">\n"
            + "<path stroke=\"sym-stroke\" fill=\"white\" pen=\"sym-pen\">\n"
            + "0 0 m\n"
            + "-1 0.333 l\n"
            + "-0.8 0 l\n"
            + "-1 -0.333 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"arrow/linear(spx)\">\n"
            + "<path stroke=\"sym-stroke\" pen=\"sym-pen\">\n"
            + "-1 0.333 m\n"
            + "0 0 l\n"
            + "-1 -0.333 l\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"arrow/fdouble(spx)\">\n"
            + "<path stroke=\"sym-stroke\" fill=\"white\" pen=\"sym-pen\">\n"
            + "0 0 m\n"
            + "-1 0.333 l\n"
            + "-1 -0.333 l\n"
            + "h\n"
            + "-1 0 m\n"
            + "-2 0.333 l\n"
            + "-2 -0.333 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<symbol name=\"arrow/double(spx)\">\n"
            + "<path stroke=\"sym-stroke\" fill=\"sym-stroke\" pen=\"sym-pen\">\n"
            + "0 0 m\n"
            + "-1 0.333 l\n"
            + "-1 -0.333 l\n"
            + "h\n"
            + "-1 0 m\n"
            + "-2 0.333 l\n"
            + "-2 -0.333 l\n"
            + "h\n"
            + "</path>\n"
            + "</symbol>\n"
            + "<pen name=\"heavier\" value=\"0.8\"/>\n"
            + "<pen name=\"fat\" value=\"1.2\"/>\n"
            + "<pen name=\"ultrafat\" value=\"2\"/>\n"
            + "<symbolsize name=\"large\" value=\"5\"/>\n"
            + "<symbolsize name=\"small\" value=\"2\"/>\n"
            + "<symbolsize name=\"tiny\" value=\"1.1\"/>\n"
            + "<arrowsize name=\"large\" value=\"10\"/>\n"
            + "<arrowsize name=\"small\" value=\"5\"/>\n"
            + "<arrowsize name=\"tiny\" value=\"3\"/>\n"
            + "<color name=\"red\" value=\"1 0 0\"/>\n"
            + "<color name=\"green\" value=\"0 1 0\"/>\n"
            + "<color name=\"blue\" value=\"0 0 1\"/>\n"
            + "<color name=\"yellow\" value=\"1 1 0\"/>\n"
            + "<color name=\"orange\" value=\"1 0.647 0\"/>\n"
            + "<color name=\"gold\" value=\"1 0.843 0\"/>\n"
            + "<color name=\"purple\" value=\"0.627 0.125 0.941\"/>\n"
            + "<color name=\"gray\" value=\"0.745\"/>\n"
            + "<color name=\"brown\" value=\"0.647 0.165 0.165\"/>\n"
            + "<color name=\"navy\" value=\"0 0 0.502\"/>\n"
            + "<color name=\"pink\" value=\"1 0.753 0.796\"/>\n"
            + "<color name=\"seagreen\" value=\"0.18 0.545 0.341\"/>\n"
            + "<color name=\"turquoise\" value=\"0.251 0.878 0.816\"/>\n"
            + "<color name=\"violet\" value=\"0.933 0.51 0.933\"/>\n"
            + "<color name=\"darkblue\" value=\"0 0 0.545\"/>\n"
            + "<color name=\"darkcyan\" value=\"0 0.545 0.545\"/>\n"
            + "<color name=\"darkgray\" value=\"0.663\"/>\n"
            + "<color name=\"darkgreen\" value=\"0 0.392 0\"/>\n"
            + "<color name=\"darkmagenta\" value=\"0.545 0 0.545\"/>\n"
            + "<color name=\"darkorange\" value=\"1 0.549 0\"/>\n"
            + "<color name=\"darkred\" value=\"0.545 0 0\"/>\n"
            + "<color name=\"lightblue\" value=\"0.678 0.847 0.902\"/>\n"
            + "<color name=\"lightcyan\" value=\"0.878 1 1\"/>\n"
            + "<color name=\"lightgray\" value=\"0.827\"/>\n"
            + "<color name=\"lightgreen\" value=\"0.565 0.933 0.565\"/>\n"
            + "<color name=\"lightyellow\" value=\"1 1 0.878\"/>\n"
            + "<dashstyle name=\"dashed\" value=\"[4] 0\"/>\n"
            + "<dashstyle name=\"dotted\" value=\"[1 3] 0\"/>\n"
            + "<dashstyle name=\"dash dotted\" value=\"[4 2 1 2] 0\"/>\n"
            + "<dashstyle name=\"dash dot dotted\" value=\"[4 2 1 2 1 2] 0\"/>\n"
            + "<textsize name=\"large\" value=\"\\large\"/>\n"
            + "<textsize name=\"small\" value=\"\\small\"/>\n"
            + "<textsize name=\"tiny\" value=\"\\tiny\"/>\n"
            + "<textsize name=\"Large\" value=\"\\Large\"/>\n"
            + "<textsize name=\"LARGE\" value=\"\\LARGE\"/>\n"
            + "<textsize name=\"huge\" value=\"\\huge\"/>\n"
            + "<textsize name=\"Huge\" value=\"\\Huge\"/>\n"
            + "<textsize name=\"footnote\" value=\"\\footnotesize\"/>\n"
            + "<textstyle name=\"center\" begin=\"\\begin{center}\" end=\"\\end{center}\"/>\n"
            + "<textstyle name=\"itemize\" begin=\"\\begin{itemize}\" end=\"\\end{itemize}\"/>\n"
            + "<textstyle name=\"item\" begin=\"\\begin{itemize}\\item{}\" end=\"\\end{itemize}\"/>\n"
            + "<gridsize name=\"4 pts\" value=\"4\"/>\n"
            + "<gridsize name=\"8 pts (~3 mm)\" value=\"8\"/>\n"
            + "<gridsize name=\"16 pts (~6 mm)\" value=\"16\"/>\n"
            + "<gridsize name=\"32 pts (~12 mm)\" value=\"32\"/>\n"
            + "<gridsize name=\"10 pts (~3.5 mm)\" value=\"10\"/>\n"
            + "<gridsize name=\"20 pts (~7 mm)\" value=\"20\"/>\n"
            + "<gridsize name=\"14 pts (~5 mm)\" value=\"14\"/>\n"
            + "<gridsize name=\"28 pts (~10 mm)\" value=\"28\"/>\n"
            + "<gridsize name=\"56 pts (~20 mm)\" value=\"56\"/>\n"
            + "<anglesize name=\"90 deg\" value=\"90\"/>\n"
            + "<anglesize name=\"60 deg\" value=\"60\"/>\n"
            + "<anglesize name=\"45 deg\" value=\"45\"/>\n"
            + "<anglesize name=\"30 deg\" value=\"30\"/>\n"
            + "<anglesize name=\"22.5 deg\" value=\"22.5\"/>\n"
            + "<tiling name=\"falling\" angle=\"-60\" step=\"4\" width=\"1\"/>\n"
            + "<tiling name=\"rising\" angle=\"30\" step=\"4\" width=\"1\"/>\n"
            + "</ipestyle>\n";
    private static final String IPE6HEADER =
            // We require IPE version 6
            "<ipe version=\"60032\" creator=\"RectangularCartogram\">\n"
            + "<info bbox=\"cropbox\"/>\n"
            + "<ipestyle>\n"
            + "</ipestyle>\n";
    private static final String LAYERS =
            // Beginning of the IPE page
            "<page>\n"
            // Layers
            + "<layer name=\"Vertices\"/>\n"
            + "<layer name=\"Edges\"/>\n"
            + "<layer name=\"Constraints\"/>\n"
            // The default view showing everything
            + "<view layers=\"Vertices Edges Constraints\" active=\"Vertices\"/>\n";
    private static final String POST_TAGS =
            "</page>\n</ipe>";

    public void exportGraph(File file, Graph graph, boolean useIPE6) throws IOException {
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(file));

            if (useIPE6) {
                out.write(IPE6HEADER);
            } else {
                out.write(IPE7HEADER);
            }

            out.write(LAYERS);

            for (Constraint c : graph.getConstraints()) {
                exportConstraint(out, c);
            }

            for (Edge e : graph.getEdges()) {
                exportEdge(out, e.getVA().getX(), e.getVA().getY(), e.getVB().getX(), e.getVB().getY());
            }

            // Vertices last, so they appear on top
            for (GraphVertex v : graph.getVertices()) {
                exportVertex(out, v.getX(), v.getY(), useIPE6);
            }

            out.write(POST_TAGS);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void exportEmbeddedGraph(File file, EmbeddedGraph graph, boolean useIPE6) throws IOException {
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(file));

            if (useIPE6) {
                out.write(IPE6HEADER);
            } else {
                out.write(IPE7HEADER);
            }

            out.write(LAYERS);

            // Keep track of which edges we already exported
            HashSet<HalfEdge> drawnHalfEdges = new HashSet<HalfEdge>(graph.getDarts().size());

            for (HalfEdge e : graph.getDarts()) {
                if (!drawnHalfEdges.contains(e)) {
                    exportEdge(out, e.getOrigin().getX(), e.getOrigin().getY(), e.getDestination().getX(), e.getDestination().getY());

                    drawnHalfEdges.add(e);
                    drawnHalfEdges.add(e.getTwin());
                }
            }

            // Vertices last, so they appear on top
            for (EmbeddedVertex v : graph.getVertices()) {
                exportVertex(out, v.getX(), v.getY(), useIPE6);
            }

            out.write(POST_TAGS);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void exportVertex(BufferedWriter out, double x, double y, boolean useIPE6) throws IOException {
        if (useIPE6) {
            out.write("<mark layer=\"Vertices\" type=\"1\" pos=\"" + x + " " + y + "\" size=\"large\" stroke=\"black\" fill=\"blue\"/>\n");
        } else {
            out.write("<use layer=\"Vertices\" name=\"mark/fdisk(sfx)\" pos=\"" + x + " " + y + "\" size=\"large\" stroke=\"black\" fill=\"blue\"/>\n");
        }
    }

    private void exportEdge(BufferedWriter out, double x1, double y1, double x2, double y2) throws IOException {
        out.write("<path layer=\"Edges\" pen=\"normal\" stroke=\"black\">\n");
        out.write(x1 + " " + y1 + " m\n");
        out.write(x2 + " " + y2 + " l\n");
        out.write("</path>\n");
    }

    private void exportConstraint(BufferedWriter out, Constraint c) throws IOException {
        out.write("<path layer=\"Constraints\" pen=\"fat\" stroke=\"black\">\n");
        out.write(c.getVA().getX() + " " + c.getVA().getY() + " m\n");
        out.write(c.getVB().getX() + " " + c.getVB().getY() + " l\n");
        out.write("</path>\n");
    }
}
