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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    private static final String NEWLINE = "\n";
    private ArrayList<GraphVertex> vertices;
    private ArrayList<Edge> edges;
    private ArrayList<Constraint> constraints;

    public Graph() {
        vertices = new ArrayList<GraphVertex>();
        edges = new ArrayList<Edge>();
        constraints = new ArrayList<Constraint>();
    }

    /**
     * Creates a new graph that is a copy of the given graph.
     *
     * @param graph
     */
    public Graph(Graph graph) {
        vertices = new ArrayList<GraphVertex>(graph.getVertices().size());
        edges = new ArrayList<Edge>(graph.getEdges().size());
        constraints = new ArrayList<Constraint>(graph.getConstraints().size());

        Map<GraphVertex, GraphVertex> vertexMap = new HashMap<GraphVertex, GraphVertex>(graph.getVertices().size() * 2);

        for (GraphVertex v : graph.getVertices()) {
            GraphVertex newV = new GraphVertex(v.getX(), v.getY(), v.isVisible());
            vertices.add(newV);
            vertexMap.put(v, newV);
        }

        for (Edge edge : graph.getEdges()) {
            GraphVertex vA = vertexMap.get(edge.getVA()), vB = vertexMap.get(edge.getVB());
            Edge e = new Edge(vA, vB);

            vA.addEdge(e);
            vB.addEdge(e);
            edges.add(e);
        }

        for (Constraint constraint : constraints) {
            constraints.add(new Constraint(vertexMap.get(constraint.getVA()), vertexMap.get(constraint.getVB())));
        }
    }

    public List<GraphVertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public ArrayList<Constraint> getConstraints() {
        return constraints;
    }

    public boolean addVertex(final GraphVertex v) {
        if (!vertices.contains(v)) {
            vertices.add(v);
            return true;
        } else {
            return false;
        }
    }

    public void addEdge(final GraphVertex vA, final GraphVertex vB) {
        addEdge(vA, vB, false);
    }

    public void addEdge(final GraphVertex vA, final GraphVertex vB, boolean directed) {
        // Don't add self-loops
        if (vA != vB) {
            Edge e = new Edge(vA, vB, directed);

            // Don't add edges that are already here
            if (directed) {
                if (vA.isAdjacentTo(vB)) {
                    return;
                }

                // If the reverse of a directed edge is already in the graph, just make the edge undirected
                Edge reverse = vB.getEdgeTo(vA);

                if (reverse != null) {
                    reverse.setDirected(false);
                    return;
                }
            } else {
                if (vA.getDegree() < vB.getDegree()) {
                    if (vA.isAdjacentTo(vB)) {
                        return;
                    }
                } else {
                    if (vB.isAdjacentTo(vA)) {
                        return;
                    }
                }
            }

            // Otherwise, add the edge
            vA.addEdge(e);
            vB.addEdge(e);
            edges.add(e);
        }
    }

    public void addConstraint(GraphVertex a, GraphVertex b) {
        constraints.add(new Constraint(a, b));
    }

    public void removeConstraint(Constraint c) {
        constraints.remove(c);
    }

    public GraphVertex getVertexAt(final double x, final double y, final double precision) {
        for (GraphVertex v : vertices) {
            if (v.isNear(x, y, precision)) {
                return v;
            }
        }
        return null;
    }

    public Edge getEdgeAt(final double x, final double y, final double precision) {
        for (Edge e : edges) {
            if (e.isNear(x, y, precision)) {
                return e;
            }
        }
        return null;
    }

    public Constraint getConstraintAt(double x, double y, double precision) {
        double precisionSq = precision * precision;

        for (Constraint c : constraints) {
            if (c.getLine().ptSegDistSq(x, y) < precisionSq) {
                return c;
            }
        }

        return null;
    }

    public void removeVertex(final GraphVertex v) {
        for (Edge e : v.getEdges()) {
            if (e.getVA() != v) {
                e.getVA().removeEdge(e);
            }

            if (e.getVB() != v) {
                e.getVB().removeEdge(e);
            }

            edges.remove(e);
        }

        vertices.remove(v);
    }

    public void removeEdge(final Edge e) {
        edges.remove(e);
        e.getVA().removeEdge(e);
        e.getVB().removeEdge(e);
    }

    public void clear() {
        vertices.clear();
        edges.clear();
    }

    public void clearEdges() {
        for (Edge e : edges) {
            e.getVA().removeEdge(e);
            e.getVB().removeEdge(e);
        }

        edges.clear();
    }

    public String toSaveString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Vertices");
        buffer.append(NEWLINE);

        buffer.append(vertices.size());
        buffer.append(NEWLINE);

        for (GraphVertex vertex : vertices) {
            buffer.append(vertex.toSaveString());
            buffer.append(NEWLINE);
        }

        buffer.append(NEWLINE);
        buffer.append("Edges");
        buffer.append(NEWLINE);

        buffer.append(edges.size());
        buffer.append(NEWLINE);

        for (Edge edge : edges) {
            // print the indices of the endpoints of this edge
            buffer.append(vertices.indexOf(edge.getVA()));
            buffer.append(" ");
            buffer.append(vertices.indexOf(edge.getVB()));
            buffer.append(NEWLINE);
        }

        buffer.append(NEWLINE);
        buffer.append("Constraints");
        buffer.append(NEWLINE);

        buffer.append(constraints.size());
        buffer.append(NEWLINE);

        for (Constraint constraint : constraints) {
            // print the indices of the endpoints of this constraint
            buffer.append(vertices.indexOf(constraint.getVA()));
            buffer.append(" ");
            buffer.append(vertices.indexOf(constraint.getVB()));
            buffer.append(NEWLINE);
        }

        return buffer.toString();
    }

    public static Graph fromSaveString(String s) throws IOException {
        String[] lines = s.split("\n");
        Graph result = new Graph();

        // Read until the first line "Vertices"
        int i = 0;

        while (i < lines.length && !"Vertices".equals(lines[i])) {
            i++;
        }

        if (i == lines.length) {
            throw new IOException("Incorrect file format");
        }

        // Read the number of vertices
        i++;
        int nVertices = Integer.parseInt(lines[i]);

        ArrayList<GraphVertex> vertices = new ArrayList<GraphVertex>(nVertices);

        // Read the vertices
        i++;
        while (i < lines.length && !"Edges".equals(lines[i])) {
            String line = lines[i].trim();

            // Skip blank lines
            if (line.length() > 0) {
                GraphVertex v = GraphVertex.fromSaveString(lines[i]);

                if (v != null) {
                    vertices.add(v);
                    result.addVertex(v);
                }
            }

            i++;
        }

        if (i == lines.length) {
            throw new IOException("Incorrect file format");
        }

        // Read the number of edges
        i++;
        int nEdges = Integer.parseInt(lines[i]);

        // Read the edges
        i++;
        while (i < lines.length && !"Constraints".equals(lines[i])) {
            String line = lines[i].trim();

            // Skip blank lines
            if (line.length() > 0) {
                String[] parts = line.split(" ");
                int v1 = Integer.parseInt(parts[0]);
                int v2 = Integer.parseInt(parts[1]);

                result.addEdge(vertices.get(v1), vertices.get(v2));
            }

            i++;
        }

        // Optional - read the constraints
        if (i < lines.length) {
            i++;
            int nConstraints = Integer.parseInt(lines[i]);

            i++;
            while (i < lines.length) {
                String line = lines[i].trim();

                // Skip blank lines
                if (line.length() > 0) {
                    String[] parts = line.split(" ");
                    int v1 = Integer.parseInt(parts[0]);
                    int v2 = Integer.parseInt(parts[1]);

                    result.addConstraint(vertices.get(v1), vertices.get(v2));
                }

                i++;
            }
        }

        return result;
    }
}
