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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GraphVertex {

    protected double x, y;
    private ArrayList<Edge> edges;
    private boolean visible;

    public GraphVertex(double x, double y) {
        this(x, y, true);
    }

    public GraphVertex(double x, double y, boolean visible) {
        this.x = x;
        this.y = y;
        this.edges = new ArrayList<Edge>();
        this.visible = visible;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public void removeEdge(Edge e) {
        edges.remove(e);
    }

    /**
     * Returns true if there is an edge from this vertex to the given vertex, taking direction into account, false otherwise.
     * @param v
     * @return
     */
    public boolean isAdjacentTo(GraphVertex v) {
        return getEdgeTo(v) != null;
    }

    /**
     * Returns the edge to the given vertex if it is a neighbour of this vertex, or null otherwise.
     * @param v
     * @return
     */
    public Edge getEdgeTo(GraphVertex v) {
        for (Edge e : edges) {
            GraphVertex neighbour;
            
            if (e.isDirected()) {
                if (e.getVB() == this) {
                    continue;
                } else {
                    neighbour = e.getVB();
                }
            } else {
                neighbour = (e.getVA() == this ? e.getVB() : e.getVA());
            }

            if (neighbour == v) {
                return e;
            }
        }

        return null;
    }

    /**
     * Returns a list of all vertices that have an edge to this vertex, irrespective of direction.
     * @return
     */
    public List<GraphVertex> getNeighbours() {
        List<GraphVertex> neighbours = new ArrayList<GraphVertex>(edges.size());

        for (Edge e : edges) {
            if (e.isDirected()) {
                if (e.getVB() == this) {
                    continue;
                } else {
                    neighbours.add(e.getVB());
                }
            } else {
                neighbours.add(e.getVA() == this ? e.getVB() : e.getVA());
            }
        }

        return neighbours;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the total number of edges that have this vertex as an endpoint. Undirected edges are counted once.
     * @return 
     */
    public int getDegree() {
        return edges.size();
    }
    
    /**
     * Returns the number of edges with this vertex as destination.
     * @return 
     */
    public int getInDegree() {
        int inDegree = 0;
        
        for (Edge edge : edges) {
            if (edge.isDirected()) {
                if (edge.getVB() == this) {
                    inDegree++;
                }
            } else {
                inDegree++;
            }
        }
        
        return inDegree;
    }
    
    /**
     * Returns the number of edges with this vertex as origin.
     * @return 
     */
    public int getOutDegree() {
        int outDegree = 0;
        
        for (Edge edge : edges) {
            if (edge.isDirected()) {
                if (edge.getVA() == this) {
                    outDegree++;
                }
            } else {
                outDegree++;
            }
        }
        
        return outDegree;
    }

    public boolean isNear(double x, double y, double precision) {
        double dX = x - this.x;
        double dY = y - this.y;
        return (dX * dX + dY * dY <= precision * precision);
    }

    public String toSaveString() {
        return x + " " + y;
    }

    public static GraphVertex fromSaveString(String s) {
        String[] parts = s.split(" ");

        double x = java.lang.Double.parseDouble(parts[0]);
        double y = java.lang.Double.parseDouble(parts[1]);

        GraphVertex v = new GraphVertex(x, y);

        return v;

    }
    
    public static final Comparator<GraphVertex> increasingX = new Comparator<GraphVertex>() {

        @Override
        public int compare(GraphVertex v1, GraphVertex v2) {
            int compX = java.lang.Double.compare(v1.getX(), v2.getX());

            if (compX != 0) {
                return compX;
            } else {
                return java.lang.Double.compare(v1.getY(), v2.getY());
            }
        }
    };

    public static final Comparator<GraphVertex> increasingY = new Comparator<GraphVertex>() {

        @Override
        public int compare(GraphVertex v1, GraphVertex v2) {
            int compY = java.lang.Double.compare(v1.getY(), v2.getY());

            if (compY != 0) {
                return compY;
            } else {
                return java.lang.Double.compare(v1.getX(), v2.getX());
            }
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphVertex other = (GraphVertex) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

    /*
     * Removed, because when storing information related to a vertex in a hash map, the information would be lost when the vertex was moved.
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }*/

    @Override
    public String toString() {
        return "V[" + x + ", " + y + "]";
    }
}
