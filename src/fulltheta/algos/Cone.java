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

public class Cone implements Comparable<Cone> {

    private double aperture;
    private double bisector;

    public Cone(double aperture, double bisector) {
        this.aperture = aperture;
        this.bisector = bisector;
    }

    /**
     * Get the aperture of this cone.
     *
     * @return the value of aperture
     */
    public double getAperture() {
        return aperture;
    }

    /**
     * Get the bisector angle of this cone. This is the clockwise angle that the
     * half-line from the apex along the bisector makes with the positive
     * y-axis.
     *
     * @return the value of bisector
     */
    public double getBisector() {
        return bisector;
    }

    /**
     * Returns the first border angle of this cone. This is the clockwise angle
     * that the half-line from the apex along the first (in clockwise order)
     * border makes with the positive y-axis. This is equivalent (mod 2 PI) to
     * <code>getBisector() - getAperture() / 2</code>.
     *
     * @return
     */
    public double getFirstBorder() {
        double angle = bisector - aperture / 2;

        if (angle < 2 * Math.PI) {
            angle += 2 * Math.PI;
        }

        return angle;
    }

    /**
     * Returns the second border angle of this cone. This is the clockwise angle
     * that the half-line from the apex along the second (in clockwise order)
     * border makes with the positive y-axis. This is equivalent (mod 2 PI) to
     * <code>getBisector() + getAperture() / 2</code>.
     *
     * @return
     */
    public double getSecondBorder() {
        double angle = bisector + aperture / 2;

        if (angle > 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }

        return angle;
    }

    /**
     * Returns true if v lies inside this cone when translated to the given
     * apex, false otherwise.
     *
     * @param apex
     * @param v
     * @return
     */
    public boolean contains(GraphVertex apex, GraphVertex v) {
        double dy = v.getY() - apex.getY();
        double dx = v.getX() - apex.getX();

        // Rotate 90 degrees, so the positive x-axis is rotated onto the positive y-axis
        double dyr = -dx;
        double dxr = dy;

        double angle = -Math.atan2(dyr, dxr); // -pi <= angle <= pi, clockwise angle from the positive y-axis

        if (angle < 0) {
            angle += 2 * Math.PI; // 0 <= angle <= 2 * pi
        }

        return contains(angle);
    }

    /**
     * Returns true if the half-line with the given angle lies inside this cone,
     * false otherwise.
     *
     * @param angle
     * @return
     */
    public boolean contains(double angle) {
        double diff1 = angle - bisector;
        double diff2 = bisector - angle;
        
        if (diff1 < 0) {
            diff1 += 2 * Math.PI;
        }
        
        if (diff2 < 0) {
            diff2 += 2 * Math.PI;
        }
        
        return Math.min(diff1, diff2) <= aperture / 2;
    }

    @Override
    public int compareTo(Cone o) {
        // Sort by bisector
        return Double.compare(bisector, o.bisector);
    }
}
