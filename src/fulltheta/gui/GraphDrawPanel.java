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
import fulltheta.algos.SpannerBuilder;
import fulltheta.data.Pair;
import fulltheta.data.graph.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;

public class GraphDrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    private final double HIT_PRECISION = 7; // How close you must click to a vertex or edge in order to select it. Higher values mean you can be further away. Note that this makes it harder to select the right vertex when several are very close.
    private final int VERTEX_SIZE = 5; // Radius in pixels of the vertices
    private Graph graph; // The current graph
    private boolean locked = false;
    private GraphVertex selectedVertex = null; // The currently selected vertex
    private Edge selectedEdge = null; // The currently selected edge.
    private Constraint selectedConstraint = null; // The currently selected constraint. Only one object can be selected at a time.
    private double zoomfactor = 1;
    private int panX = 0;
    private int panY = 0;
    private int mouseX = 0;
    private int mouseY = 0;
    private Collection<SelectionListener> listeners;
    private Cones cones = new Cones(false, 4);
    private List<GraphVertex> markedVertices;
    private List<GraphVertex> negMarkedVertices;
    private GraphVertex canonicalApex;
    private GraphVertex canonicalEnd;
    private List<Edge> highlightPath;
    private List<List<Pair<Double, Double>>> emptyRegions;
    private boolean autoUpdate = true;
    private boolean directed = false;
    private boolean drawCones = true;

    public GraphDrawPanel() {
        initialize();
    }

    private void initialize() {
        setFocusable(true);
        setOpaque(true);
        setBackground(Color.white);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);

        graph = new Graph();
        listeners = new ArrayList<SelectionListener>();
        markedVertices = new ArrayList<GraphVertex>();
        negMarkedVertices = new ArrayList<GraphVertex>();
        canonicalApex = null;
        canonicalEnd = null;
        highlightPath = null;
        emptyRegions = null;
    }

    public void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        listeners.remove(listener);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        setSelectedVertex(null);
        markedVertices.clear();
        negMarkedVertices.clear();
        highlightPath = null;
        canonicalApex = null;
        canonicalEnd = null;
        emptyRegions = null;
        zoomToGraph();

        if (cones != null) {
            cones.setVertices(graph.getVertices());
        }
    }

    /**
     * Change the graph, while keeping as many settings the same as before, to
     * not make the transition as jarring.
     *
     * @param newGraph
     */
    public void softChangeGraph(Graph newGraph, boolean updateView, boolean updateVertices, boolean updateEdges, boolean clearEmptyRegions) {
        this.graph = newGraph;

        if (updateVertices) {
            deselectVertex();
            markedVertices.clear();
            negMarkedVertices.clear();
            canonicalApex = null;
            canonicalEnd = null;
            cones.setVertices(graph.getVertices());
        }

        if (updateEdges) {
            deselectEdge();
            highlightPath = null;
        }

        if (updateView) {
            zoomToGraph();
        }

        if (clearEmptyRegions) {
            emptyRegions = null;
        }

        repaint();
    }

    public void recomputeSpanner() {
        deselectEdge();
        highlightPath = null;

        SpannerBuilder.buildConeSpanner(graph, cones, directed);

        repaint();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void toggleMarked(GraphVertex v) {
        if (!markedVertices.remove(v)) {
            // v was not marked, was it negmarked?
            if (!negMarkedVertices.remove(v)) {
                markedVertices.add(v);
            }
        } else {
            // marked -> negmarked
            negMarkedVertices.add(v);
        }
    }

    public void clearMarkedVertices() {
        markedVertices.clear();
        negMarkedVertices.clear();
        repaint();
    }

    public Edge getSelectedEdge() {
        return selectedEdge;
    }

    public GraphVertex getSelectedVertex() {
        return selectedVertex;
    }

    public void deleteSelection() {
        if (selectedVertex != null) {
            graph.removeVertex(selectedVertex);
            markedVertices.remove(selectedVertex); // Remove it if it is present
            negMarkedVertices.remove(selectedVertex);
            deselectVertex();

            if (cones != null) {
                recomputeSpanner();
            }
        } else if (selectedEdge != null) {
            graph.removeEdge(selectedEdge);
            deselectEdge();
        } else if (selectedConstraint != null) {
            graph.removeConstraint(selectedConstraint);
            deselectConstraint();
        }

        repaint();
    }

    public Cones getCones() {
        return cones;
    }

    public void setCones(Cones cones) {
        this.cones = cones;
        recomputeSpanner();
    }

    public List<Edge> getHighlightPath() {
        return highlightPath;
    }

    public void setHighlightPath(List<Edge> highlightPath) {
        this.highlightPath = highlightPath;
        repaint();
    }

    public List<List<Pair<Double, Double>>> getEmptyRegions() {
        return emptyRegions;
    }

    public void setEmptyRegions(List<List<Pair<Double, Double>>> emptyRegions) {
        this.emptyRegions = emptyRegions;
    }

    public void addEmptyRegion(List<Pair<Double, Double>> region) {
        if (emptyRegions == null) {
            emptyRegions = new ArrayList<List<Pair<Double, Double>>>();
        }

        emptyRegions.add(region);
    }

    public GraphVertex getCanonicalApex() {
        return canonicalApex;
    }

    public void setCanonicalApex(GraphVertex canonicalApex) {
        this.canonicalApex = canonicalApex;
    }

    public GraphVertex getCanonicalEnd() {
        return canonicalEnd;
    }

    public void setCanonicalEnd(GraphVertex canonicalEnd) {
        this.canonicalEnd = canonicalEnd;
    }

    public List<GraphVertex> getMarkedVertices() {
        return markedVertices;
    }

    public void setMarkedVertices(List<GraphVertex> markedVertices) {
        this.markedVertices = markedVertices;
    }

    public List<GraphVertex> getNegMarkedVertices() {
        return negMarkedVertices;
    }

    public void setNegMarkedVertices(List<GraphVertex> negMarkedVertices) {
        this.negMarkedVertices = negMarkedVertices;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
        recomputeSpanner();
    }
    
    public void setDrawCones(boolean draw) {
        drawCones = draw;
        repaint();
    }

    public void zoomToGraph() {
        if (graph != null && !graph.getVertices().isEmpty()) {
            int margin = 20;

            double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY,
                    maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

            for (GraphVertex vertex : graph.getVertices()) {
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

        repaint();
    }

    protected double xScreenToWorld(int x) {
        return (x + panX) * zoomfactor;
    }

    protected double yScreenToWorld(int y) {
        return (getHeight() - y + panY) * zoomfactor;
    }

    protected int xWorldToScreen(double x) {
        return (int) Math.round((x / zoomfactor) - panX);
    }

    protected int yWorldToScreen(double y) {
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

        // Draw empty regions
        if (emptyRegions != null) {
            g.setColor(new Color(220, 220, 220)); // A light gray

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
            if (v != selectedVertex) {
                // Thin grey lines
                g.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke());

                drawCones(v, g);
            }
        }

        // Draw cones for negatively marked vertices
        for (GraphVertex v : negMarkedVertices) {
            // Thin grey lines
            g.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke());

            drawCones(v, g, false);
        }

        // Draw cones for the selected vertex
        if (selectedVertex != null && drawCones) {
            // Thin red lines
            g.setColor(Color.RED);
            g2.setStroke(new BasicStroke());

            drawCones(selectedVertex, g);
        }

        // Draw constraints
        for (Constraint constraint : graph.getConstraints()) {
            if (constraint == selectedConstraint) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.BLACK);
            }

            g2.setStroke(new BasicStroke(4));

            g.drawLine(xWorldToScreen(constraint.getVA().getX()), yWorldToScreen(constraint.getVA().getY()), xWorldToScreen(constraint.getVB().getX()), yWorldToScreen(constraint.getVB().getY()));
        }
        
        // Draw the canonical triangle
        if (canonicalApex != null && canonicalEnd != null) {
            Cone cone = cones.getCone(canonicalApex, canonicalEnd);

            if (cone != null) {
                drawCanonicalRegion(canonicalApex, canonicalEnd, cone, (Graphics2D) g);
            }
        }

        // Draw all edges
        g2.setStroke(new BasicStroke());
        g.setColor(Color.BLACK);

        for (Edge e : graph.getEdges()) {
            if (e.isVisible()) {
                drawEdge(g2, e);
            }
        }

        // Draw special edges
        if (highlightPath != null) {
            g2.setStroke(new BasicStroke(2));
            g.setColor(Color.ORANGE);

            for (Edge e : highlightPath) {
                drawEdge(g2, e);
            }
        }

        if (selectedEdge != null) {
            g2.setStroke(new BasicStroke(2));
            g.setColor(Color.RED);

            drawEdge(g2, selectedEdge);
        }

        // Draw the vertices
        for (GraphVertex v : graph.getVertices()) {
            if (v.isVisible()) {
                g.setColor(Color.white);
                g.fillOval(xWorldToScreen(v.getX()) - VERTEX_SIZE, yWorldToScreen(v.getY()) - VERTEX_SIZE, 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);

                if (v == selectedVertex) {
                    g.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(2));
                } else if (v == canonicalApex || v == canonicalEnd) {
                    g.setColor(Color.BLUE);
                    g2.setStroke(new BasicStroke(2));
                } else {
                    g.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke());
                }

                g.drawOval(xWorldToScreen(v.getX()) - VERTEX_SIZE, yWorldToScreen(v.getY()) - VERTEX_SIZE, 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);
            }
        }

        if (selectedVertex != null) {
            // Draw the closest vertex in each cone
            g.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke());

            for (Cone cone : cones.getCones()) {
                GraphVertex closest = cones.getClosestVertexInCone(selectedVertex, cone);

                if (closest != null) {
                    g.fillOval(xWorldToScreen(closest.getX()) - VERTEX_SIZE, yWorldToScreen(closest.getY()) - VERTEX_SIZE, 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);
                }
            }
        }
    }

    private void drawEdge(Graphics2D g2, Edge e) {
        GraphVertex vA = e.getVA();
        GraphVertex vB = e.getVB();

        // Draw the edge
        g2.drawLine(xWorldToScreen(vA.getX()), yWorldToScreen(vA.getY()), xWorldToScreen(vB.getX()), yWorldToScreen(vB.getY()));

        if (e.isDirected()) {
            // Draw an arrowhead in the middle
            drawHead(g2, xWorldToScreen(vA.getX()), yWorldToScreen(vA.getY()), xWorldToScreen(0.5 * (vA.getX() + vB.getX())), yWorldToScreen(0.5 * (vA.getY() + vB.getY())));
        }
    }

    private void drawHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
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
        g2.draw(arrowHead);
    }

    protected void drawCones(GraphVertex v, Graphics g) {
        drawCones(v, g, true);
    }

    protected void drawCones(GraphVertex v, Graphics g, boolean positive) {
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

        // Start straight up
        if (!positive) { // Or straight down if we're drawing negative cones
            g2.rotate(Math.PI);
        }

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

    private void drawCanonicalRegion(GraphVertex apex, GraphVertex end, Cone cone, Graphics2D g) {
        g.setColor(Color.blue);
        g.setStroke(new BasicStroke(2));

        if (cones.isTheta()) {
            drawCanonicalTriangle(new CanonicalTriangle(apex, end, cones), g);
        } else {
            int xA = xWorldToScreen(apex.getX());
            int yA = yWorldToScreen(apex.getY());
            int xB = xWorldToScreen(end.getX());
            int yB = yWorldToScreen(end.getY());

            double radius = Math.sqrt((xA - xB) * (xA - xB) + (yA - yB) * (yA - yB));
            double startAngle = toDegrees(getStartAngle(cone));
            double arcAngle = toDegrees(cone.getAperture());

            Arc2D arc = new Arc2D.Double();
            arc.setArcByCenter(xA, yA, radius, startAngle, arcAngle, Arc2D.PIE);
            g.draw(arc);
        }
    }

    private double getStartAngle(Cone cone) {
        double result = Math.PI / 2 - (cone.getBisector() + 0.5 * cone.getAperture());

        if (result < 0) {
            result += 2 * Math.PI;
        }

        return result;
    }

    /**
     * Converts the given angle in radians to degrees.
     *
     * @param theta
     * @return
     */
    private double toDegrees(double theta) {
        return theta * 180 / Math.PI;
    }

    private void drawCanonicalTriangle(CanonicalTriangle c, Graphics2D g) {
        g.drawLine(xWorldToScreen(c.getApex().getX()), yWorldToScreen(c.getApex().getY()), xWorldToScreen(c.getCorner1X()), yWorldToScreen(c.getCorner1Y()));
        g.drawLine(xWorldToScreen(c.getApex().getX()), yWorldToScreen(c.getApex().getY()), xWorldToScreen(c.getCorner2X()), yWorldToScreen(c.getCorner2Y()));

        g.drawLine(xWorldToScreen(c.getCorner1X()), yWorldToScreen(c.getCorner1Y()), xWorldToScreen(c.getCorner2X()), yWorldToScreen(c.getCorner2Y()));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        double wX = xScreenToWorld(e.getX());
        double wY = yScreenToWorld(e.getY());

        GraphVertex v = graph.getVertexAt(wX, wY, zoomfactor * HIT_PRECISION);

        if (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()) {
            if (!locked && selectedVertex != null && v != null) {
                // Add a constraint
                graph.addConstraint(selectedVertex, v);
                repaint();
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (v == null) {
                setSelectedVertex(null);
            }

            repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
            double wX = xScreenToWorld(e.getX());
            double wY = yScreenToWorld(e.getY());

            GraphVertex v = graph.getVertexAt(wX, wY, zoomfactor * HIT_PRECISION);

            if (v == null) {
                // Check if we selected a constraint
                Constraint constraint = graph.getConstraintAt(wX, wY, zoomfactor * HIT_PRECISION);

                if (constraint == null) {
                    // Check if we selected an edge
                    Edge edge = graph.getEdgeAt(wX, wY, zoomfactor * HIT_PRECISION);

                    if (edge == null) {
                        if (locked) {
                            setSelectedVertex(null);
                        } else {
                            GraphVertex newVertex = new GraphVertex(wX, wY);
                            graph.addVertex(newVertex);
                            setSelectedVertex(newVertex);

                            if (cones != null) {
                                if (autoUpdate) {
                                    recomputeSpanner();
                                } else {
                                    cones.setVertices(graph.getVertices());
                                }
                            }
                        }
                    } else {
                        setSelectedEdge(edge);
                    }
                } else {
                    setSelectedConstraint(constraint);
                }
            } else {
                setSelectedVertex(v);
            }

            repaint();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // start panning, store the current mouse position
            mouseX = e.getX();
            mouseY = e.getY();

            // Check if we clicked a vertex
            double wX = xScreenToWorld(e.getX());
            double wY = yScreenToWorld(e.getY());

            GraphVertex v = graph.getVertexAt(wX, wY, zoomfactor * HIT_PRECISION);

            if (v != null) {
                if (canonicalApex == null || canonicalEnd != null) {
                    canonicalApex = v;
                    canonicalEnd = null;
                } else {
                    canonicalEnd = v;
                }

                repaint();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
            // pan
            panX += mouseX - e.getX();
            panY += e.getY() - mouseY;

            mouseX = e.getX();
            mouseY = e.getY();

            repaint();
        } else if (!locked && selectedVertex != null) {
            selectedVertex.setX(xScreenToWorld(e.getX()));
            selectedVertex.setY(yScreenToWorld(e.getY()));

            if (cones != null) {
                if (autoUpdate) {
                    recomputeSpanner();
                } else {
                    cones.setVertices(graph.getVertices());
                }
            }

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double factor;

        if (e.getWheelRotation() < 0) {
            factor = (10.0 / 11.0);
        } else {
            factor = (11.0 / 10.0);
        }

        zoomfactor *= factor;

        int centerX = e.getX();
        int centerY = getHeight() - e.getY();
        panX = (int) Math.round((centerX + panX) / factor - centerX);
        panY = (int) Math.round((centerY + panY) / factor - centerY);

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!locked && (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_D)) {
            deleteSelection();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            zoomToGraph();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void setSelectedVertex(GraphVertex v) {
        deselectEdge();
        deselectConstraint();

        if (v != selectedVertex) {
            selectedVertex = v;

            for (SelectionListener list : listeners) {
                list.vertexSelected(this, v);
            }

            requestFocus();
        }
    }

    public void setSelectedEdge(Edge e) {
        deselectVertex();
        deselectConstraint();

        if (e != selectedEdge) {
            selectedEdge = e;

            for (SelectionListener list : listeners) {
                list.edgeSelected(this, e);
            }

            requestFocus();
        }
    }

    public void setSelectedConstraint(Constraint c) {
        deselectVertex();
        deselectEdge();

        if (c != selectedConstraint) {
            selectedConstraint = c;

            for (SelectionListener list : listeners) {
                list.constraintSelected(this, c);
            }

            requestFocus();
        }
    }

    public void deselectVertex() {
        // Deselect the current selected vertex
        if (selectedVertex != null) {
            selectedVertex = null;

            for (SelectionListener list : listeners) {
                list.vertexSelected(this, null);
            }
        }
    }

    public void deselectEdge() {
        // Deselect the current selected edge
        if (selectedEdge != null) {
            selectedEdge = null;

            for (SelectionListener list : listeners) {
                list.edgeSelected(this, null);
            }
        }
    }

    public void deselectConstraint() {
        // Deselect the current selected constraint
        if (selectedConstraint != null) {
            selectedConstraint = null;

            for (SelectionListener list : listeners) {
                list.constraintSelected(this, null);
            }
        }
    }
}
