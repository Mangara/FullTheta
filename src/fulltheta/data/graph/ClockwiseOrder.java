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

import java.util.Comparator;

public class ClockwiseOrder implements Comparator<Edge> {

    private GraphVertex center;

    public ClockwiseOrder(GraphVertex center) {
        this.center = center;
    }

    public int compare(Edge e1, Edge e2) {
        // compare the angles of e1 and e2
        return Double.compare(getAngle(e1), getAngle(e2));
    }

    private double getAngle(Edge e) {
        GraphVertex dest = (e.getVA() == center ? e.getVB() : e.getVA());

        double vx = dest.getX() - center.getX();
        double vy = dest.getY() - center.getY();

        double angle = Math.acos(vy / Math.sqrt(vx * vx + vy * vy));

        if (vx > 0) {
            return angle;
        } else {
            return -angle;
        }
    }
}
