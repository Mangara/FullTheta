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
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class ConeDrawPanel extends GraphDrawPanel {

    public ConeDrawPanel() {
        removeMouseListener(this);
        removeMouseMotionListener(this);
        removeMouseWheelListener(this);
        removeKeyListener(this);
        
        GraphVertex a = new GraphVertex(0, 0);
        GraphVertex b = new GraphVertex(1, 10);
        
        Graph graph = getGraph();
        graph.addVertex(a);
        graph.addVertex(b);
        
        setCanonicalApex(a);
        setCanonicalEnd(b);
        
        setSelectedVertex(a);
    }

    private static final Color CONE_BOUNDARY = Color.red;
    //private static final Color CONE_FILL = new Color(0, 128, 0, 42); // Transparent
    private static final Color CONE_FILL = new Color(45, 215, 0); // Opaque
    
    @Override
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
        
        // Construct all cones
        Cone[] cones = getCones().getCones();
        List<Shape> coneShapes = new ArrayList<Shape>(cones.length);
        
        for (Cone cone : cones) {
            double coneX1 = 2 * length * Math.sin(cone.getAperture() / 2);
            double coneX2 = -coneX1;
            double coneY = -2 * length * Math.cos(cone.getAperture() / 2);
            
            Path2D coneShape = new Path2D.Double();
            coneShape.moveTo(coneX1, coneY);
            coneShape.lineTo(0, 0);
            coneShape.lineTo(coneX2, coneY);
            
            coneShapes.add(coneShape);
        }
        
        // First fill all cones
        AffineTransform start = g2.getTransform();
        
        for (int i = 0; i < cones.length; i++) {
            g2.rotate(cones[i].getBisector());
            
            g2.setColor(CONE_FILL);
            g2.fill(coneShapes.get(i));
            
            g2.setTransform(start);
        }

        // Then draw all cone boundaries
        for (int i = 0; i < cones.length; i++) {
            g2.rotate(cones[i].getBisector());
            
            g2.setColor(CONE_BOUNDARY);
            g2.draw(coneShapes.get(i));
            
            g2.setTransform(start);
        }

        g2.setTransform(current);
    }

    @Override
    public void zoomToGraph() {
        GraphVertex temp = new GraphVertex(-1, -10); // Symmetric from b
        getGraph().addVertex(temp);
        super.zoomToGraph();
        getGraph().removeVertex(temp);
    }

    @Override
    protected void paintComponent(Graphics g) {
        zoomToGraph();
        super.paintComponent(g);
    }
}
