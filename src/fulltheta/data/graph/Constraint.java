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

public class Constraint {
    private GraphVertex vA, vB;

    public Constraint(GraphVertex vA, GraphVertex vB) {
        this.vA = vA;
        this.vB = vB;
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

    public Line2D getLine() {
        return new Line2D.Double(vA.x, vA.y, vB.x, vB.y);
    }
}
