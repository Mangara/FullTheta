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

public class HalfEdge {

    public static final double epsilon = 0.00000000001;
    private Face face;
    private HalfEdge twin;
    private HalfEdge next;
    private HalfEdge previous;
    private EmbeddedVertex origin;

    public Face getFace() {
        return face;
    }

    public void setFace(Face face) {
        this.face = face;
    }

    public HalfEdge getNext() {
        return next;
    }

    public void setNext(HalfEdge next) {
        this.next = next;
    }

    public EmbeddedVertex getOrigin() {
        return origin;
    }

    public void setOrigin(EmbeddedVertex origin) {
        this.origin = origin;
    }

    public HalfEdge getPrevious() {
        return previous;
    }

    public void setPrevious(HalfEdge previous) {
        this.previous = previous;
    }

    public HalfEdge getTwin() {
        return twin;
    }

    public void setTwin(HalfEdge twin) {
        this.twin = twin;
    }

    public EmbeddedVertex getDestination() {
        return twin.getOrigin();
    }

    public double getLength() {
        double dx = getDestination().getX() - origin.getX();
        double dy = getDestination().getY() - origin.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return origin + " -> " + getDestination();
    }
}
