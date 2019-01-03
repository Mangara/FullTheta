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

import fulltheta.algos.Cone;
import fulltheta.algos.Cones;
import fulltheta.data.Pair;
import java.util.ArrayList;
import java.util.List;

public class CanonicalTriangle {

    private GraphVertex apex, end;
    private double corner1X, corner1Y, corner2X, corner2Y;

    /**
     * Pre-condition: <code>cones</code> contains at least one cone that when translated to have apex <code>apex</code>, contains <code>end</code>.
     * @param apex
     * @param end
     * @param cones 
     */
    public CanonicalTriangle(GraphVertex apex, GraphVertex end, Cones cones) {
        this.apex = apex;
        this.end = end;
        Cone cone = cones.getCone(apex, end);

        // Auxilliary variables
        double theta = cone.getAperture();
        double tip = Math.tan(cone.getBisector() + theta / 2);
        double tim = Math.tan(cone.getBisector() - theta / 2);

        if (cone.getBisector() == Math.PI / 2 || cone.getBisector() == 3 * Math.PI / 2) {
            // The line perpendicular to the bisector is vertical.

            // The lines along the boundary of cone i have the following formula:
            //   y = ay + (x - ax) / tan(i * theta +/- theta / 2),
            // where (ax, ay) is the apex and theta is the angle of a cone.
            // The bisector has the formula
            //   y = ay.
            // So the line perpendicular to the bisector, going through the end point (vx, vy), has formula
            //   x = vx.
            // So we can just substitute vx in these formulas to get the coordinates.
            
            corner1X = end.getX();
            corner1Y = apex.getY() + (corner1X - apex.getX()) / tip;

            corner2X = end.getX();
            corner2Y = apex.getY() + (corner2X - apex.getX()) / tim;
        } else {
            // The lines along the boundary of cone i have the following formula:
            //   y = ay + (x - ax) / tan(i * theta +/- theta / 2),
            // where (ax, ay) is the apex and theta is the angle of a cone.
            // The bisector has the formula
            //   y = ay + (x - ax) / tan(i * theta).
            // So a line perpendicular to the bisector has slope
            //   -1 / (1 / tan(i * theta)) = -tan(i * theta).
            // It needs to go through the end point (vx, vy), so it has the formula
            //   y = vy + tan(i * theta) * (vx - x).
            // The rest is just computing the intersection points of these lines.

            double ti = Math.tan(cone.getBisector());

            corner1X = (end.getY() - apex.getY() + ti * end.getX() + apex.getX() / tip) / (ti + 1 / tip);
            corner1Y = end.getY() + ti * (end.getX() - corner1X);

            corner2X = (end.getY() - apex.getY() + ti * end.getX() + apex.getX() / tim) / (ti + 1 / tim);
            corner2Y = end.getY() + ti * (end.getX() - corner2X);
        }
    }

    public GraphVertex getApex() {
        return apex;
    }

    public double getCorner1X() {
        return corner1X;
    }

    public double getCorner1Y() {
        return corner1Y;
    }

    public GraphVertex getCorner1() {
        return new GraphVertex(corner1X, corner1Y);
    }
    
    public double getCorner2X() {
        return corner2X;
    }

    public double getCorner2Y() {
        return corner2Y;
    }
    
    public GraphVertex getCorner2() {
        return new GraphVertex(corner2X, corner2Y);
    }

    public GraphVertex getEnd() {
        return end;
    }

    public List<Pair<Double, Double>> toCoordinateList() {
        List<Pair<Double, Double>> coordinates = new ArrayList<Pair<Double, Double>>();

        coordinates.add(new Pair<Double, Double>(apex.getX(), apex.getY()));
        coordinates.add(new Pair<Double, Double>(corner1X, corner1Y));
        coordinates.add(new Pair<Double, Double>(corner2X, corner2Y));

        return coordinates;
    }
}
