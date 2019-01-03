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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Face {

    private HalfEdge dart;
    private Set<Face> mergedFaces;

    public Face() {
        mergedFaces = new HashSet<Face>();
    }

    public HalfEdge getDart() {
        return dart;
    }

    public void setDart(final HalfEdge dart) {
        this.dart = dart;
    }

    public int getVertexCount() {
        int cnt = 1;
        HalfEdge drt = dart.getNext();
        while (drt != dart) {
            drt = drt.getNext();
            cnt++;
        }
        return cnt;
    }

    public List<HalfEdge> getDarts() {
        List<HalfEdge> darts = new ArrayList<HalfEdge>();
        HalfEdge drt = dart;
        do {
            darts.add(drt);
            drt = drt.getNext();
        } while (drt != dart);
        return darts;
    }

    public List<HalfEdge> getMergedDarts() {
        List<HalfEdge> darts = getDarts();
        for (Face f: mergedFaces) {
            darts.addAll(f.getDarts());
        }
        return darts;
    }

    public Set<Face> getMergedFaces() {
        return mergedFaces;
    }
}
