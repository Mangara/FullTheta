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
package fulltheta.algos;

import fulltheta.data.graph.GraphVertex;
import java.util.*;

public class Cones {

    private final boolean simple; // Is it possible to generate these with the simple tab of the ConesDialog?
    private final boolean theta;
    private final Cone[] cones; // Sorted by bisctor angle
    private Map<Cone, List<GraphVertex>> sortedVertices;

    /**
     * Create a new set of Cones consisting of the given cones.
     *
     * @param theta
     * @param cones
     */
    public Cones(boolean theta, Cone[] cones) {
        // It might be possible to generate these, but this is easier.
        // Plus, it guarantees that the user will return to the advanced tab the next time they open the ConesDialog
        this.simple = false;
        this.theta = theta;
        this.cones = cones;
        Collections.sort(Arrays.asList(this.cones));
        sortedVertices = null;
    }

    /**
     * Creates a new set of Cones representing a Theta- or Yao-graph with the
     * given number of cones.
     *
     * @param theta
     * @param half
     * @param nCones
     */
    public Cones(boolean theta, boolean half, int nCones) {
        this.simple = true;
        this.theta = theta;
        sortedVertices = null;

        double t = 2 * Math.PI / nCones;
        double delta;

        if (half) {
            cones = new Cone[(nCones + 1) / 2];
            delta = 2 * t;
        } else {
            cones = new Cone[nCones];
            delta = t;
        }

        for (int i = 0; i < cones.length; i++) {
            cones[i] = new Cone(t, i * delta);
        }
    }

    /**
     * Creates a new set of Cones representing a full Theta- or Yao-graph with
     * the given number of cones.
     *
     * @param theta
     * @param nCones
     */
    public Cones(boolean theta, int nCones) {
        this(theta, false, nCones);
    }

    /**
     * Were these cones generated by the simple tab of ConesDialog?
     * @return 
     */
    public boolean isSimple() {
        return simple;
    }

    /**
     * Returns true if the closest vertex is chosen by projecting onto the
     * bisector of each cone, and false if it is chosen by Euclidean distance.
     *
     * @return
     */
    public boolean isTheta() {
        return theta;
    }

    /**
     * Returns the cones in this set.
     *
     * @return
     */
    public Cone[] getCones() {
        return cones;
    }

    /**
     * Pre-processes the given vertex set for cone queries.
     *
     * @param vertices
     */
    public void setVertices(List<GraphVertex> vertices) {
        sortedVertices = new HashMap<Cone, List<GraphVertex>>(cones.length * 2);

        for (int i = 0; i < cones.length; i++) {
            List<GraphVertex> sortedCone = new ArrayList<GraphVertex>(vertices);
            Collections.sort(sortedCone, new Cones.ProjectionOrder(cones[i]));
            sortedVertices.put(cones[i], sortedCone);
        }
    }

    /**
     * Returns the cone from this set that contains the given vertex if
     * translated to the given apex, or
     * <code>null</code> if no such cone exists. If multiple such cones exist,
     * the one with the smallest bisector angle is returned.
     *
     * @param apex
     * @param v
     * @return
     */
    public Cone getCone(GraphVertex apex, GraphVertex v) {
        double dy = v.getY() - apex.getY();
        double dx = v.getX() - apex.getX();

        // Rotate 90 degrees, so the positive x-axis is rotated onto the positive y-axis
        double dyr = -dx;
        double dxr = dy;

        double angle = -Math.atan2(dyr, dxr); // -pi <= angle <= pi, clockwise angle from the positive y-axis

        if (angle < 0) {
            angle += 2 * Math.PI; // 0 <= angle <= 2 * pi
        }

        for (int i = 0; i < cones.length; i++) {
            if (cones[i].contains(angle)) {
                return cones[i];
            }
        }

        return null;
    }

    /**
     * Returns a list containing the closest vertex in each cone in this set,
     * when translated to have apex v.
     *
     * @param v
     * @return
     */
    public List<GraphVertex> getClosestVertices(GraphVertex v) {
        List<GraphVertex> vertices = new ArrayList<GraphVertex>(cones.length);

        for (Cone cone : cones) {
            GraphVertex closest = getClosestVertexInCone(v, cone);

            if (closest != null) {
                vertices.add(closest);
            }
        }

        return vertices;
    }

    /**
     * Returns the closest vertex in the specified cone translated to the given
     * apex. The distance measure depends on whether this represents a
     * theta-graph or not.
     *
     * @param apex
     * @param cone
     * @return
     */
    public GraphVertex getClosestVertexInCone(GraphVertex apex, Cone cone) {
        if (sortedVertices == null) {
            System.err.println("SortedVertices is null!");
            return null;
        } else {
            List<GraphVertex> vertices = sortedVertices.get(cone);
            ProjectionOrder projection = new Cones.ProjectionOrder(cone);

            // Binary search for apex
            int index = findApex(vertices, apex, projection);

            if (index >= 0) {
                if (theta) {
                    for (int i = index + 1; i < vertices.size(); i++) {
                        if (cone.contains(apex, vertices.get(i))) {
                            return vertices.get(i);
                        }
                    }
                } else {
                    double minDistSq = Double.POSITIVE_INFINITY;
                    double minDist = Double.POSITIVE_INFINITY;
                    GraphVertex closest = null;

                    for (int i = index + 1; i < vertices.size(); i++) {
                        if (closest != null && projection.getProjectionValue(vertices.get(i)) - projection.getProjectionValue(apex) > minDist) {
                            // We won't see any closer points
                            break;
                        } else if (cone.contains(apex, vertices.get(i))) {
                            double dx = apex.getX() - vertices.get(i).getX();
                            double dy = apex.getY() - vertices.get(i).getY();
                            double distSq = dx * dx + dy * dy;

                            if (distSq < minDistSq) {
                                minDistSq = distSq;
                                minDist = Math.sqrt(minDistSq);
                                closest = vertices.get(i);
                            }
                        }
                    }

                    return closest;
                }

                // No vertex in this cone
                return null;
            } else {
                System.err.println("Apex is not in the current vertex set.");
                return null;
            }
        }
    }

    /**
     * Uses binary search to find the index of the apex in the list of vertices.
     * Note that the vertices should be sorted using the given ProjectionOrder.
     *
     * @param vertices
     * @param apex
     * @param projection
     * @return
     */
    private int findApex(List<GraphVertex> vertices, GraphVertex apex, ProjectionOrder projection) {
        int index = Collections.binarySearch(vertices, apex, projection);

        if (index >= 0 && vertices.get(index) != apex) { // Correct for non-general position
            // Linear search in the interval with the same projection value
            double value = projection.getProjectionValue(apex);

            while (index > 0 && vertices.get(index) != apex && projection.getProjectionValue(vertices.get(index)) == value) {
                index--;
            }

            while (index < vertices.size() && vertices.get(index) != apex) { // The apex should always be in here, so we can skip the projection value check
                index++;
            }

            if (vertices.get(index) != apex) {
                System.err.println("Apex is not in the current vertex set. (Although vertices with the same projection value are.)");
                return -1;
            }
        }

        return index;
    }

    private class ProjectionOrder implements Comparator<GraphVertex> {

        private double xf;
        private double yf;

        ProjectionOrder(Cone cone) {
            xf = Math.sin(cone.getBisector());
            yf = Math.cos(cone.getBisector());
        }

        @Override
        public int compare(GraphVertex o1, GraphVertex o2) {
            return Double.compare(getProjectionValue(o1), getProjectionValue(o2));
        }

        public double getProjectionValue(GraphVertex v) {
            return xf * v.getX() + yf * v.getY();
        }
    }
}