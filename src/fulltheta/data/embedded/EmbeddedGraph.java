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
package fulltheta.data.embedded;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import fulltheta.data.graph.ClockwiseOrder;
import fulltheta.data.graph.Edge;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;

public class EmbeddedGraph {

    private Set<EmbeddedVertex> vertices;
    private Set<Face> faces;
    private Set<HalfEdge> darts;

    public EmbeddedGraph(final Graph graph, final double max_length_sqrd) {
        vertices = new HashSet<EmbeddedVertex>();
        faces = new HashSet<Face>();
        darts = new HashSet<HalfEdge>();

        HashMap<GraphVertex, EmbeddedVertex> vertexMap = new HashMap<GraphVertex, EmbeddedVertex>();
        HashMap<Edge, HalfEdge> edgeMap = new HashMap<Edge, HalfEdge>();

        // make vertices
        for (GraphVertex vertex : graph.getVertices()) {
            EmbeddedVertex vtx = new EmbeddedVertex(vertex.getX(), vertex.getY());
            vertices.add(vtx);
            vertexMap.put(vertex, vtx);
        }

        // make darts
        for (Edge edge : graph.getEdges()) {
            EmbeddedVertex emvA = vertexMap.get(edge.getVA());
            EmbeddedVertex emvB = vertexMap.get(edge.getVB());

            HalfEdge d1 = new HalfEdge();
            d1.setOrigin(emvA);
            emvA.setDart(d1);
            darts.add(d1);

            HalfEdge d2 = new HalfEdge();
            d2.setOrigin(emvB);
            emvB.setDart(d2);
            darts.add(d2);

            d2.setTwin(d1);
            d1.setTwin(d2);

            edgeMap.put(edge, d1);
        }

        // For each vertex, find the cyclic order of outgoing darts
        // The next vertex in the cyclic order is the next of your twin
        // So we can compute the next of all incoming darts and the previous of all outgoing darts this way
        for (GraphVertex vertex : graph.getVertices()) {
            // Sort the edges of this vertex in cyclic (clockwise) order
            ArrayList<Edge> edges = new ArrayList<Edge>(vertex.getEdges());
            Collections.sort(edges, new ClockwiseOrder(vertex));

            ArrayList<HalfEdge> outgoingDarts = new ArrayList<HalfEdge>(edges.size());

            for (Edge edge : edges) {
                // Find the outgoing dart that corresponds to this edge
                HalfEdge outgoing = edgeMap.get(edge);

                if (outgoing.getOrigin() != vertexMap.get(vertex)) {
                    outgoing = outgoing.getTwin();
                }

                outgoingDarts.add(outgoing);
            }

            for (int i = 0; i < outgoingDarts.size(); i++) {
                // The next of the twin of this dart is the next dart in clockwise order around the vertex
                HalfEdge dart = outgoingDarts.get(i);
                HalfEdge nextDart = outgoingDarts.get((i + 1) % outgoingDarts.size());

                dart.getTwin().setNext(nextDart);
                nextDart.setPrevious(dart.getTwin());
            }
        }

        // make and set faces
        for (HalfEdge dart : darts) {
            if (dart.getFace() == null) {
                Face face = new Face();

                face.setDart(dart);
                dart.setFace(face);

                HalfEdge walkDart = dart.getNext();
                while (walkDart != dart) {
                    walkDart.setFace(face);
                    walkDart = walkDart.getNext();
                }

                faces.add(face);
            }
        }
    }

    public Set<EmbeddedVertex> getVertices() {
        return vertices;
    }

    public void setDarts(final Set<HalfEdge> darts) {
        this.darts = darts;
    }

    public void setFaces(final Set<Face> faces) {
        this.faces = faces;
    }

    public void setVertices(final Set<EmbeddedVertex> vertices) {
        this.vertices = vertices;
    }

    public void addVertex(final EmbeddedVertex v) {
        vertices.add(v);
    }

    public Set<Face> getFaces() {
        return faces;
    }

    public void addFace(final Face f) {
        faces.add(f);
    }

    public Set<HalfEdge> getDarts() {
        return darts;
    }

    public void addDart(final HalfEdge d) {
        darts.add(d);
    }

    public boolean verifyDCEL() {
        System.out.println("Starting verification");
        // for all darts, the twin of the twin must be the dart itself
        // and the previous of the next must be itself
        for (HalfEdge e : darts) {
            HalfEdge t = e.getTwin();
            if (t == null) {
                System.out.println("----> No twin");
                return false;
            } else if (t.getTwin() != e) {
                System.out.println("----> Twins twin is not edge");
                return false;
            } else if (t == e) {
                System.out.println("----> edge is its own twin");
                return false;
            }

            HalfEdge n = e.getNext();
            if (n == null) {
                System.out.println("----> No next");
                return false;
            } else if (n.getPrevious() != e) {
                System.out.println("----> Nexts previous is not edge");
                return false;
            } else if (n == e) {
                System.out.println("----> edge is its own next");
                return false;
            }

            HalfEdge p = e.getPrevious();
            if (p == null) {
                System.out.println("----> No previous");
                return false;
            } else if (p.getNext() != e) {
                System.out.println("----> Previous' next is not edge");
                return false;
            } else if (p == e) {
                System.out.println("----> edge is its own previous");
                return false;
            }

            if (!e.getOrigin().getEdges().contains(e)) {
                System.out.println("----> origins edges do not contain edge");
                return false;
            }
        }

        // for each vertex, the source of its edge must be the vertex itself
        // as for all other edges around
        for (EmbeddedVertex v : vertices) {
            HalfEdge e = v.getDart();

            if (e == null) {
                System.out.println("----> No edge for vertex");
                return false;
            } else {
                HalfEdge d = e;

                do {
                    if (d.getOrigin() == null) {
                        System.out.println("----> No origin");
                        return false;
                    } else if (d.getOrigin() != v) {
                        System.out.println("----> Incorrect origin");
                        return false;
                    }

                    d = d.getTwin().getNext();
                } while (e != d);
            }
        }

        // for each face, the face of its edge must be the face itself
        // as for all other edge around the face
        for (Face f : faces) {
            HalfEdge e = f.getDart();
            if (e == null) {
                System.out.println("----> No edge for face");
                return false;
            } else {
                HalfEdge d = e;
                do {
                    if (d.getFace() != f) {
                        System.out.println("----> Incorrect face");
                        return false;
                    }
                    d = d.getNext();
                } while (e != d);
            }
        }

        System.out.println("Verification succesful");
        return true;
    }
}
