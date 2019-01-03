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
package fulltheta.gui;

import fulltheta.algos.Cone;
import fulltheta.algos.Cones;
import fulltheta.data.Pair;
import fulltheta.data.graph.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.List;
import javax.swing.JPanel;

public class EdgeRemovalDrawPanel extends JPanel {

    private final int VERTEX_SIZE = 5; // Radius in pixels of the vertices
    private Graph graph; // The current graph
    private Edge removedEdge = null; // The currently selected edge.
    private double zoomfactor = 1;
    private int panX = 0;
    private int panY = 0;
    private Cones cones = null;
    private List<GraphVertex> markedVertices;
    private GraphVertex canonicalApex;
    private GraphVertex canonicalEnd;
    private GraphVertex canonicalApex2;
    private GraphVertex canonicalEnd2;
    private List<List<Pair<Double, Double>>> emptyRegions;
    private List<GraphVertex> zoomVertices;
    private List<GraphVertex> addedVertices;

    public EdgeRemovalDrawPanel(GraphDrawPanel gdp, Graph g, Edge removedEdge, List<GraphVertex> zoomVertices, List<GraphVertex> addedVertices) {
        setFocusable(true);
        setOpaque(true);
        setBackground(Color.white);

        graph = g;
        markedVertices = gdp.getMarkedVertices();
        emptyRegions = gdp.getEmptyRegions();
        cones = gdp.getCones();
        
        this.removedEdge = removedEdge;
        canonicalApex = removedEdge.getVA();
        canonicalEnd = removedEdge.getVB();
        canonicalApex2 = removedEdge.getVB();
        canonicalEnd2 = removedEdge.getVA();
        
        this.zoomVertices = zoomVertices;
        this.addedVertices = addedVertices;
    }
    
    private void zoomToVertices(List<GraphVertex> zoomVertices) {
        if (zoomVertices != null && !zoomVertices.isEmpty()) {
            int margin = 10;

            double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY,
                    maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

            for (GraphVertex vertex : zoomVertices) {
                minX = Math.min(minX, vertex.getX());
                minY = Math.min(minY, vertex.getY());
                maxX = Math.max(maxX, vertex.getX());
                maxY = Math.max(maxY, vertex.getY());
            }

            double zoomfactorX = (maxX - minX) / (getWidth() - 2 * margin);
            double zoomfactorY = (maxY - minY) / (getHeight() - 2 * margin);

            if (zoomfactorY > zoomfactorX) {
                zoomfactor = zoomfactorY;
                panX = (int) Math.round((maxX + minX) / (2 * zoomfactor)) - getWidth() / 2;
                panY = (int) Math.round(maxY / zoomfactor) - getHeight() + margin;
            } else {
                zoomfactor = zoomfactorX;
                panX = (int) Math.round(minX / zoomfactor) - margin;
                panY = (int) Math.round((maxY + minY) / (2 * zoomfactor)) - getHeight() / 2;
            }
        }
    }

    private int xWorldToScreen(double x) {
        return (int) Math.round((x / zoomfactor) - panX);
    }

    private int yWorldToScreen(double y) {
        return getHeight() - (int) Math.round((y / zoomfactor) - panY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Clear the screen
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Turn on anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Focus on the right area of the graph
        zoomToVertices(zoomVertices);
        
        // Draw empty regions
        if (emptyRegions != null) {
            g.setColor(new Color(198, 198, 198)); // A light gray

            for (List<Pair<Double, Double>> p : emptyRegions) {
                if (p != null && !p.isEmpty()) {
                    GeneralPath gp = new GeneralPath();
                    gp.moveTo(xWorldToScreen(p.get(0).getFirst()), yWorldToScreen(p.get(0).getSecond()));

                    for (int i = 1; i < p.size(); i++) {
                        gp.lineTo(xWorldToScreen(p.get(i).getFirst()), yWorldToScreen(p.get(i).getSecond()));
                    }

                    gp.closePath();

                    g2.fill(gp);
                }
            }
        }

        // Draw cones for marked vertices
        for (GraphVertex v : markedVertices) {
            // Thin grey lines
            g.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke());

            drawCones(v, g);
        }

        // Draw the canonical triangles
        if (canonicalApex != null && canonicalEnd != null) {
            drawCanonicalTriangle(canonicalApex, canonicalEnd, (Graphics2D) g);
        }

        if (canonicalApex2 != null && canonicalEnd2 != null) {
            drawCanonicalTriangle(canonicalApex2, canonicalEnd2, (Graphics2D) g);
        }

        // Draw constraints
        g.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4));

        for (Constraint constraint : graph.getConstraints()) {
            g.drawLine(xWorldToScreen(constraint.getVA().getX()), yWorldToScreen(constraint.getVA().getY()), xWorldToScreen(constraint.getVB().getX()), yWorldToScreen(constraint.getVB().getY()));
        }

        // Draw the removed edge
        if (removedEdge != null) {
            g2.setColor(Color.red);
            //g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{10f}, 0));
            g2.setStroke(new BasicStroke(2));
            
            drawEdge(removedEdge, g);
        }

        // Draw all edges
        g2.setStroke(new BasicStroke());
        g.setColor(Color.BLACK);

        for (Edge e : graph.getEdges()) {
            if (e.isVisible()) {
                drawEdge(e, g);
            }
        }

        // Draw the vertices
        for (GraphVertex v : graph.getVertices()) {
            if (v.isVisible()) {
                g.setColor(Color.white);
                g.fillOval(xWorldToScreen(v.getX()) - VERTEX_SIZE, yWorldToScreen(v.getY()) - VERTEX_SIZE, 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);

                if (v == canonicalApex || v == canonicalEnd) {
                    g.setColor(Color.BLUE);
                    g2.setStroke(new BasicStroke(2));
                } else if (addedVertices != null && addedVertices.contains(v)) {
                    g.setColor(new Color(0, 164, 0));
                    g2.setStroke(new BasicStroke(2));
                } else {
                    g.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke());
                }

                g.drawOval(xWorldToScreen(v.getX()) - VERTEX_SIZE, yWorldToScreen(v.getY()) - VERTEX_SIZE, 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);
            }
        }
    }

    private void drawEdge(Edge e, Graphics g) {
        GraphVertex vA = e.getVA();
        GraphVertex vB = e.getVB();

        // Draw the edge
        g.drawLine(xWorldToScreen(vA.getX()), yWorldToScreen(vA.getY()), xWorldToScreen(vB.getX()), yWorldToScreen(vB.getY()));

        if (e.isDirected()) {
            // Draw an arrowhead in the middle
            drawHead(g, xWorldToScreen(vA.getX()), yWorldToScreen(vA.getY()), xWorldToScreen(0.5 * (vA.getX() + vB.getX())), yWorldToScreen(0.5 * (vA.getY() + vB.getY())));
        }
    }

    private void drawHead(Graphics g, int x1, int y1, int x2, int y2) {
        int ARROW_HEAD_WIDTH = 8;
        int ARROW_HEAD_LENGTH = 8;

        // Calculate the vector
        double vx = x2 - x1;
        double vy = y2 - y1;

        // Normalize it
        double length = Math.sqrt(vx * vx + vy * vy);

        vx /= length;
        vy /= length;

        // Get the orthogonal vector to the right
        double ox = vy;
        double oy = -vx;

        // Calculate the two points
        double leftPointX = x2 - ARROW_HEAD_LENGTH * vx - ARROW_HEAD_WIDTH * ox;
        double leftPointY = y2 - ARROW_HEAD_LENGTH * vy - ARROW_HEAD_WIDTH * oy;
        double rightPointX = x2 - ARROW_HEAD_LENGTH * vx + ARROW_HEAD_WIDTH * ox;
        double rightPointY = y2 - ARROW_HEAD_LENGTH * vy + ARROW_HEAD_WIDTH * oy;

        // Create a GeneralPath for the arrowhead
        GeneralPath arrowHead = new GeneralPath();
        arrowHead.moveTo(leftPointX, leftPointY);
        arrowHead.lineTo(x2, y2);
        arrowHead.lineTo(rightPointX, rightPointY);

        // Draw this path
        ((Graphics2D) g).draw(arrowHead);
    }

    private void drawCones(GraphVertex v, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform current = g2.getTransform();

        int x = xWorldToScreen(v.getX());
        int y = yWorldToScreen(v.getY());

        // Make sure the cones are always visible, even if the vertex is not on the screen
        int length = Math.max(Math.max(
                Math.abs(x) + Math.abs(y), // Manhattan distance to the top left corner
                Math.abs(x - getWidth()) + Math.abs(y)), // Top right
                Math.max(
                Math.abs(x) + Math.abs(y - getHeight()), // Bottom left
                Math.abs(x - getWidth()) + Math.abs(y - getHeight()))); // Bottom right

        g2.translate(x, y); // Center at the apex

        AffineTransform start = g2.getTransform();

        for (Cone cone : cones.getCones()) {
            g2.rotate(cone.getBisector() - cone.getAperture() / 2);
            g2.drawLine(0, 0, 0, -length); // Draw a line straight upwards
            g2.rotate(cone.getAperture());
            g2.drawLine(0, 0, 0, -length); // Draw a line straight upwards
            g2.setTransform(start);
        }

        g2.setTransform(current);
    }

    private void drawCanonicalTriangle(GraphVertex apex, GraphVertex end, Graphics2D g) {
        drawCanonicalTriangle(new CanonicalTriangle(apex, end, cones), g);
    }
    
    private void drawCanonicalTriangle(CanonicalTriangle c, Graphics2D g) {
        g.setColor(Color.blue);
        g.setStroke(new BasicStroke(2));
        
        g.drawLine(xWorldToScreen(c.getApex().getX()), yWorldToScreen(c.getApex().getY()), xWorldToScreen(c.getCorner1X()), yWorldToScreen(c.getCorner1Y()));
        g.drawLine(xWorldToScreen(c.getApex().getX()), yWorldToScreen(c.getApex().getY()), xWorldToScreen(c.getCorner2X()), yWorldToScreen(c.getCorner2Y()));
        g.drawLine(xWorldToScreen(c.getCorner1X()), yWorldToScreen(c.getCorner1Y()), xWorldToScreen(c.getCorner2X()), yWorldToScreen(c.getCorner2Y()));
    }
}
