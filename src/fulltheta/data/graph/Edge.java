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
package fulltheta.data.graph;

import java.awt.geom.Line2D;

public class Edge {

    private GraphVertex vA, vB;
    private boolean directed;
    private boolean visible;

    public Edge(GraphVertex vA, GraphVertex vB) {
        this(vA, vB, false, true);
    }

    public Edge(GraphVertex vA, GraphVertex vB, boolean directed) {
        this(vA, vB, directed, true);
    }

    public Edge(GraphVertex vA, GraphVertex vB, boolean directed, boolean visible) {
        this.vA = vA;
        this.vB = vB;
        this.directed = directed;
        this.visible = visible;
    }

    public GraphVertex getVA() {
        return vA;
    }

    public void setVA(GraphVertex vA) {
        this.vA = vA;
    }

    public GraphVertex getVB() {
        return vB;
    }

    public void setVB(GraphVertex vB) {
        this.vB = vB;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the length of this edge.
     */
    public double getLength() {
        double dx = vB.getX() - vA.getX();
        double dy = vB.getY() - vA.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isNear(double x, double y, double precision) {
        double vecX = vB.getX() - vA.getX();
        double vecY = vB.getY() - vA.getY();
        double len = Math.sqrt(vecX * vecX + vecY * vecY);
        double unitX = vecX / len;
        double unitY = vecY / len;
        double offsetX = x - vA.getX();
        double offsetY = y - vA.getY();
        double dot_offset_unit = offsetX * unitX + offsetY * unitY;

        if (0 <= dot_offset_unit && dot_offset_unit <= len) {
            double rotatedUnitX = -unitY;
            double rotatedUnitY = unitX;
            double dot_offset_rotatedUnit = offsetX * rotatedUnitX + offsetY * rotatedUnitY;

            return Math.abs(dot_offset_rotatedUnit) <= precision;
        } else {
            return false;
        }
    }

    /**
     * Returns true if this edge intersects the given edge at any point, false
     * otherwise.
     *
     * @param edge
     * @return
     */
    public boolean intersects(Edge edge) {
        return Line2D.linesIntersect(vA.getX(), vA.getY(), vB.getX(), vB.getY(), edge.getVA().getX(), edge.getVA().getY(), edge.getVB().getX(), edge.getVB().getY());
    }

    /**
     * Returns true if this edge intersects the given edge, but does not share
     * any endpoint.
     *
     * @param edge
     * @return
     */
    public boolean intersectsProperly(Edge edge) {
        if (vA == edge.getVA() || vA == edge.getVB() || vB == edge.getVA() || vB == edge.getVB()) {
            return false;
        } else {
            return Line2D.linesIntersect(vA.getX(), vA.getY(), vB.getX(), vB.getY(), edge.getVA().getX(), edge.getVA().getY(), edge.getVB().getX(), edge.getVB().getY());
        }
    }

    public boolean isIncidentTo(GraphVertex v) {
        return v == vA || v == vB;
    }

    @Override
    public boolean equals(final Object obj) {
        // edges might be undirected:
        // edge a-b == edge b-a
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge other = (Edge) obj;
        if (this.vA == other.vA && this.vB == other.vB && this.directed == other.directed) {
            return true;
        }
        if (!directed && !other.directed && this.vA == other.vB && this.vB == other.vA) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        // edges might be undirected:
        // edge a-b == edge b-a
        // we take a lexicographical ordering on vertices to keep this function consistent with equals
        if (directed || this.vA.getX() < this.vB.getX() || (this.vA.getX() == this.vB.getX() && this.vA.getY() < this.vB.getY())) {
            hash = 23 * hash + (this.vA != null ? this.vA.hashCode() : 0);
            hash = 23 * hash + (this.vB != null ? this.vB.hashCode() : 0);
        } else {
            hash = 23 * hash + (this.vB != null ? this.vB.hashCode() : 0);
            hash = 23 * hash + (this.vA != null ? this.vA.hashCode() : 0);
        }
        return hash;
    }

    @Override
    public String toString() {
        return "E[" + vA + ", " + vB + "]";
    }
}
