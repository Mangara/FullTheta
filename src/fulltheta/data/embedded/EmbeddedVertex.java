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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class EmbeddedVertex {

    private HalfEdge dart;
    private double x, y;

    public EmbeddedVertex(final double x, final double y) {
        this.x = x;
        this.y = y;
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

    public HalfEdge getDart() {
        return dart;
    }

    public void setDart(final HalfEdge dart) {
        this.dart = dart;
    }

    public Collection<HalfEdge> getEdges() {
        Collection<HalfEdge> result = new ArrayList<HalfEdge>();

        HalfEdge e = dart;

        do {
            result.add(e);
            e = e.getTwin().getNext();
        } while (e != dart);

        return result;
    }

    public Set<Face> getFaces() {
        Set<Face> set = new HashSet<Face>();

        HalfEdge drt = dart;

        do {
            set.add(drt.getFace());
            drt = drt.getPrevious().getTwin();
        } while (drt != dart);

        return set;
    }

    public static final Comparator<EmbeddedVertex> increasingX = new Comparator<EmbeddedVertex>() {

        public int compare(EmbeddedVertex v1, EmbeddedVertex v2) {
            int compX = java.lang.Double.compare(v1.getX(), v2.getX());

            if (compX != 0) {
                return compX;
            } else {
                return java.lang.Double.compare(v1.getY(), v2.getY());
            }
        }
    };

    public static final Comparator<EmbeddedVertex> increasingY = new Comparator<EmbeddedVertex>() {

        public int compare(EmbeddedVertex v1, EmbeddedVertex v2) {
            int compY = java.lang.Double.compare(v1.getY(), v2.getY());

            if (compY != 0) {
                return compY;
            } else {
                return java.lang.Double.compare(v1.getX(), v2.getX());
            }
        }
    };

    @Override
    public String toString() {
        return "V[" + x + ", " + y + "]";
    }
}
