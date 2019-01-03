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

import fulltheta.algos.SpanningRatioComputer;
import fulltheta.data.graph.Graph;
import fulltheta.data.graph.GraphVertex;
import fulltheta.ipe.IPEExporter;
import fulltheta.ipe.IPEImporter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends javax.swing.JFrame {

    public GraphDrawPanel drawPanel;
    private JFileChooser openFileChooser;
    private JFileChooser saveFileChooser;
    private String myExtension = "grp";
    private FileNameExtensionFilter myFilter = new FileNameExtensionFilter("Graphs", myExtension);
    private String ipeExtension = "ipe";
    private FileNameExtensionFilter ipeFilter = new FileNameExtensionFilter("IPE XML", ipeExtension);
    private FileNameExtensionFilter ipe6Filter = new FileNameExtensionFilter("IPE 6 XML", ipeExtension);
    private FileNameExtensionFilter ipe7Filter = new FileNameExtensionFilter("IPE 7 XML", ipeExtension);
    private IPEExporter ipeExporter = new IPEExporter();
    private IPEImporter ipeImporter = new IPEImporter();
    private ConesDialog conesDialog;

    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();

        drawPanel = new GraphDrawPanel();
        drawPanel.setPreferredSize(new Dimension(1200, 600));
        centerPanel.add(drawPanel, BorderLayout.CENTER);

        // Initialize the file choosers
        openFileChooser = new JFileChooser(System.getProperty("user.dir"));
        openFileChooser.addChoosableFileFilter(myFilter);
        openFileChooser.addChoosableFileFilter(ipeFilter);
        saveFileChooser = new JFileChooser(System.getProperty("user.dir"));
        saveFileChooser.addChoosableFileFilter(myFilter);
        saveFileChooser.addChoosableFileFilter(ipe6Filter);
        saveFileChooser.addChoosableFileFilter(ipe7Filter);
        saveFileChooser.setFileFilter(ipe7Filter);
        
        conesDialog = new ConesDialog(this, drawPanel);
    }

    private Graph loadGraph(File file) throws IOException {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(file));

            StringBuilder text = new StringBuilder();
            String line = in.readLine();

            while (line != null) {
                text.append(line);
                text.append("\n");

                line = in.readLine();
            }

            // Read the data
            return Graph.fromSaveString(text.toString());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void save(File file) throws IOException {
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(file));

            // Write the data
            out.write(drawPanel.getGraph().toSaveString());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        centerPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        graphMenu = new javax.swing.JMenu();
        conesMenuItem = new javax.swing.JMenuItem();
        AutoUpdateMenuItem = new javax.swing.JCheckBoxMenuItem();
        lockMenuItem = new javax.swing.JCheckBoxMenuItem();
        buildMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        randomGraphMenuItem = new javax.swing.JMenuItem();
        markMenuItem = new javax.swing.JMenuItem();
        clearMarksMenuItem = new javax.swing.JMenuItem();
        spanningRatioMenu = new javax.swing.JMenu();
        spanningRatioMenuItem = new javax.swing.JMenuItem();
        removeEdgeMenuItem = new javax.swing.JMenuItem();
        visualizationMenu = new javax.swing.JMenu();
        directedMenuItem = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Theta Graph Explorer");

        centerPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        centerPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newMenuItem);

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("Save...");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        menuBar.add(fileMenu);

        graphMenu.setText("Graph");

        conesMenuItem.setText("Change Cones...");
        conesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conesMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(conesMenuItem);

        AutoUpdateMenuItem.setSelected(true);
        AutoUpdateMenuItem.setText("Auto-update");
        AutoUpdateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AutoUpdateMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(AutoUpdateMenuItem);

        lockMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, 0));
        lockMenuItem.setText("Graph Locked");
        lockMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lockMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(lockMenuItem);

        buildMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        buildMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, 0));
        buildMenuItem.setText("Build Spanner");
        buildMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(buildMenuItem);

        deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0));
        deleteMenuItem.setText("Delete Selection");
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(deleteMenuItem);

        randomGraphMenuItem.setText("Generate Random");
        randomGraphMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                randomGraphMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(randomGraphMenuItem);

        markMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, 0));
        markMenuItem.setText("Mark Vertex");
        markMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(markMenuItem);

        clearMarksMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        clearMarksMenuItem.setText("Clear Marks");
        clearMarksMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMarksMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(clearMarksMenuItem);

        menuBar.add(graphMenu);

        spanningRatioMenu.setText("Spanning Ratio");

        spanningRatioMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        spanningRatioMenuItem.setText("Spanning Ratio");
        spanningRatioMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spanningRatioMenuItemActionPerformed(evt);
            }
        });
        spanningRatioMenu.add(spanningRatioMenuItem);

        removeEdgeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0));
        removeEdgeMenuItem.setText("Remove Edge...");
        removeEdgeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeEdgeMenuItemActionPerformed(evt);
            }
        });
        spanningRatioMenu.add(removeEdgeMenuItem);

        menuBar.add(spanningRatioMenu);

        visualizationMenu.setText("Visualization");

        directedMenuItem.setText("Directed Edges");
        directedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directedMenuItemActionPerformed(evt);
            }
        });
        visualizationMenu.add(directedMenuItem);

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("Cones");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        visualizationMenu.add(jCheckBoxMenuItem1);

        menuBar.add(visualizationMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        int opened = openFileChooser.showOpenDialog(this);

        if (opened == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = openFileChooser.getSelectedFile();

                Graph graph = null;

                if (openFileChooser.getFileFilter() == myFilter) {
                    graph = loadGraph(selectedFile);
                } else if (openFileChooser.getFileFilter() == ipeFilter) {
                    graph = ipeImporter.importGraph(selectedFile);
                } else {
                    if (selectedFile.getName().contains("." + myExtension)) {
                        graph = loadGraph(selectedFile);
                    } else if (selectedFile.getName().contains("." + ipeExtension)) {
                        graph = ipeImporter.importGraph(selectedFile);
                    } else {
                        throw new IOException("Unknown file type.");
                    }
                }

                drawPanel.setGraph(graph);

                saveFileChooser.setCurrentDirectory(selectedFile);
            } catch (IOException ioe) {
                // Nice error
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "An error occurred while loading the data:\n"
                        + ioe.getMessage(),
                        "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        int saved = saveFileChooser.showSaveDialog(this);

        if (saved == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = saveFileChooser.getSelectedFile();

                // Add an extension if that wasn't done already and save the current graph in the correct format
                if (saveFileChooser.getFileFilter() == myFilter) {
                    if (!selectedFile.getName().contains("." + myExtension)) {
                        selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + "." + myExtension);
                    }

                    save(selectedFile);
                } else if (saveFileChooser.getFileFilter() == ipe6Filter) {
                    if (!selectedFile.getName().contains("." + ipeExtension)) {
                        selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + "." + ipeExtension);
                    }

                    ipeExporter.exportGraph(selectedFile, drawPanel.getGraph(), true);
                } else if (saveFileChooser.getFileFilter() == ipe7Filter) {
                    if (!selectedFile.getName().contains("." + ipeExtension)) {
                        selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + "." + ipeExtension);
                    }

                    ipeExporter.exportGraph(selectedFile, drawPanel.getGraph(), false);
                } else if (selectedFile.getName().contains("." + ipeExtension)) {
                    ipeExporter.exportGraph(selectedFile, drawPanel.getGraph(), false);
                } else {
                    if (!selectedFile.getName().contains(".")) {
                        selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + "." + myExtension);
                    }

                    save(selectedFile);
                }

                openFileChooser.setCurrentDirectory(selectedFile);
            } catch (IOException ioe) {
                // Nice error
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "An error occurred while saving the data:\n"
                        + ioe.getMessage(),
                        "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        drawPanel.setGraph(new Graph());
        
        if (lockMenuItem.isSelected()) {
            drawPanel.setLocked(false);
            lockMenuItem.setSelected(false);
        }
    }//GEN-LAST:event_newMenuItemActionPerformed

    private void buildMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildMenuItemActionPerformed
        drawPanel.recomputeSpanner();
    }//GEN-LAST:event_buildMenuItemActionPerformed

    private void lockMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lockMenuItemActionPerformed
        drawPanel.setLocked(lockMenuItem.isSelected());
    }//GEN-LAST:event_lockMenuItemActionPerformed

    private void randomGraphMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_randomGraphMenuItemActionPerformed
        Graph graph = new Graph();

        for (int i = 0; i < 250; i++) {
            graph.addVertex(new GraphVertex(Math.random(), Math.random()));
        }

        drawPanel.setGraph(graph);
        drawPanel.recomputeSpanner();
}//GEN-LAST:event_randomGraphMenuItemActionPerformed

    private void conesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conesMenuItemActionPerformed
        showConesDialog();
    }//GEN-LAST:event_conesMenuItemActionPerformed

    public void showConesDialog() {
        conesDialog.setCones(drawPanel.getCones());
        conesDialog.setVisible(true);
    }
    
    private void markMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markMenuItemActionPerformed
        GraphVertex v = drawPanel.getSelectedVertex();
        
        if (v != null) {
            drawPanel.toggleMarked(v);
            drawPanel.repaint();
        }
    }//GEN-LAST:event_markMenuItemActionPerformed

    private void clearMarksMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMarksMenuItemActionPerformed
        drawPanel.clearMarkedVertices();
    }//GEN-LAST:event_clearMarksMenuItemActionPerformed

    private void spanningRatioMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spanningRatioMenuItemActionPerformed
        SpanningRatioComputer sr = new SpanningRatioComputer(drawPanel.getGraph());
        drawPanel.setHighlightPath(sr.getMaximalPath());
        JOptionPane.showMessageDialog(this, "Spanning Ratio: " + sr.getSpanningRatio());
    }//GEN-LAST:event_spanningRatioMenuItemActionPerformed

    private void removeEdgeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEdgeMenuItemActionPerformed
        EdgeRemovalDialog erd = new EdgeRemovalDialog(this, drawPanel.getSelectedEdge(), drawPanel);
        erd.setVisible(true);
    }//GEN-LAST:event_removeEdgeMenuItemActionPerformed

    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
        drawPanel.deleteSelection();
    }//GEN-LAST:event_deleteMenuItemActionPerformed

    private void AutoUpdateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AutoUpdateMenuItemActionPerformed
        drawPanel.setAutoUpdate(AutoUpdateMenuItem.isSelected());
    }//GEN-LAST:event_AutoUpdateMenuItemActionPerformed

    private void directedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directedMenuItemActionPerformed
        drawPanel.setDirected(directedMenuItem.isSelected());
    }//GEN-LAST:event_directedMenuItemActionPerformed

    private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed
        drawPanel.setDrawCones(jCheckBoxMenuItem1.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem AutoUpdateMenuItem;
    private javax.swing.JMenuItem buildMenuItem;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JMenuItem clearMarksMenuItem;
    private javax.swing.JMenuItem conesMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JCheckBoxMenuItem directedMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu graphMenu;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem lockMenuItem;
    private javax.swing.JMenuItem markMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem randomGraphMenuItem;
    private javax.swing.JMenuItem removeEdgeMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenu spanningRatioMenu;
    private javax.swing.JMenuItem spanningRatioMenuItem;
    private javax.swing.JMenu visualizationMenu;
    // End of variables declaration//GEN-END:variables
}