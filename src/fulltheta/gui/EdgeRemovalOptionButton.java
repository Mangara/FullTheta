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
package fulltheta.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.*;

public class EdgeRemovalOptionButton extends JButton {
    /**
     * If the change in spanning ratio is larger than this (in percent of the previous), the text is colored.
     */
    public static final int colourBoundary = 1;

    public EdgeRemovalOptionButton(EdgeRemovalDrawPanel drawPanel, int srChange) {
        super();
        
        // Change the background color of the button and remove the margins
        setBackground(Color.white);
        setMargin(new Insets(0, 0, 0, 0));
        
        // Create a panel to hold the things we want to show on the button
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBackground(Color.white);
        
        // Add the graph
        content.add(drawPanel);
        
        // Add a label for the change in spanning ratio
        JLabel srLabel = new JLabel(srChange + "%", SwingConstants.CENTER);
        srLabel.setBackground(Color.white);
        srLabel.setOpaque(true);
        
        if (srChange > colourBoundary) {
            srLabel.setForeground(new Color(0, 164, 0));
        } else if (srChange < -colourBoundary) {
            srLabel.setForeground(new Color(164, 0, 0));
        }
        
        srLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        content.add(srLabel);
        
        // Add everything to the button
        add(content);
    }
    
}
