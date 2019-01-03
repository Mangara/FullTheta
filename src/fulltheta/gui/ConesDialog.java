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

import fulltheta.algos.Cone;
import fulltheta.algos.Cones;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ConesDialog extends javax.swing.JDialog {

    // Simple tab
    private boolean simpleTheta;
    private boolean simpleHalf;
    private int simpleNCones;
    // Advanced tab - Type
    private boolean advTheta;
    // Advanced tab - Cone angle
    private boolean advConeRadians;
    private int advConeRadiansValue;
    private double advConeDegreeValue;
    // Advanced tab - Bisectors
    private boolean advBisectorMultiples;
    private boolean advBisectorMultiplesRadians;
    private int advBisectorMultiplesRadiansValue;
    private double advBisectorMultiplesDegreeValue;
    private List<Double> advBisectorAngleDegreeValue;
    private Cones simpleCones; // The cones represented by the current state of the simple tab
    private Cones advancedCones; // The cones represented by the current state of the advanced tab
    private GraphDrawPanel drawPanel;
    private ConeDrawPanel conePreview;
    // Internal state
    private boolean updatingUI = false; // Change events are not acted on when this is true
    private boolean editingDegreeCones = false;
    private boolean editingDegreeMultipleBisectors = false;
    private boolean editingAngle = false;

    /**
     * Creates new form ConesDialog
     */
    public ConesDialog(java.awt.Frame parent, GraphDrawPanel drawPanel) {
        super(parent, false);
        initComponents();

        // Add the cone preview panel
        conePreview = new ConeDrawPanel();
        conePreview.setBorder(BorderFactory.createLoweredBevelBorder());
        previewPanel.setLayout(new BorderLayout());
        previewPanel.add(conePreview, BorderLayout.CENTER);

        // Center
        this.setBounds(parent.getX() + (parent.getWidth() - getWidth()) / 2, parent.getY() + (parent.getHeight() - getHeight()) / 2, getWidth(), getHeight());

        // Make hitting enter click OK
        getRootPane().setDefaultButton(okButton);
        okButton.requestFocusInWindow();

        this.drawPanel = drawPanel;
    }

    public void setCones(Cones cones) {
        if (cones.isSimple()) {
            // Switch to the simple tab
            modeTabbedPane.setSelectedComponent(simplePanel);

            // Update both simple and advanced controls to the new cones
            // Computing simple values
            double theta = cones.getCones()[0].getAperture();
            int ncones = cones.getCones().length;

            simpleTheta = cones.isTheta();
            simpleHalf = !equal(cones.getCones()[1].getBisector(), theta);

            if (simpleHalf) {
                if (equal(theta, Math.PI / ncones)) {
                    simpleNCones = 2 * ncones;
                } else { // Odd number of cones
                    simpleNCones = 2 * ncones - 1;
                }
            } else {
                simpleNCones = ncones;
            }

            // Computing advanced values
            advTheta = simpleTheta;

            advConeRadians = true;
            advConeRadiansValue = simpleNCones;
            advConeDegreeValue = toDegrees(theta);

            advBisectorMultiples = true;
            advBisectorMultiplesRadians = true;
            if (simpleHalf) {
                advBisectorMultiplesRadiansValue = simpleNCones * 2;
                advBisectorMultiplesDegreeValue = toDegrees(2 * Math.PI / advBisectorMultiplesRadiansValue);
            } else {
                advBisectorMultiplesRadiansValue = simpleNCones;
                advBisectorMultiplesDegreeValue = toDegrees(2 * Math.PI / advBisectorMultiplesRadiansValue);
            }

            advBisectorAngleDegreeValue = new ArrayList<Double>(simpleNCones);

            for (int i = 0; i < simpleNCones; i++) {
                advBisectorAngleDegreeValue.add(toDegrees(i * theta));

                if (simpleHalf) {
                    i++;
                }
            }

            simpleCones = cones;
            advancedCones = cones;
            updateSimpleControls();
            updateAdvancedControls();
        } else {
            // Switch to the advanced tab
            modeTabbedPane.setSelectedComponent(advancedPanel);

            // Don't update simple cones or controls, as they can't accurately display these
            simpleTheta = cones.isTheta(); // Do update the type, as that'll most likely be the same

            // Type
            advTheta = cones.isTheta();

            // Cone angle
            double theta = cones.getCones()[0].getAperture();
            int x = twoPiBy(theta);

            if (x > 1) {
                advConeRadians = true;
                advConeRadiansValue = x;
                advConeDegreeValue = toDegrees(theta);
            } else {
                advConeRadians = false;
                advConeRadiansValue = (int) Math.round(2 * Math.PI / theta);
                advConeDegreeValue = toDegrees(theta);
            }

            // Bisectors
            List<Double> bisectors = new ArrayList<Double>(cones.getCones().length);

            for (Cone cone : cones.getCones()) {
                bisectors.add(cone.getBisector());
            }

            if (areMultiples(bisectors)) {
                advBisectorMultiples = true;

                int y = twoPiBy(bisectors.get(1));

                if (y > 1) {
                    advBisectorMultiplesRadians = true;
                    advBisectorMultiplesRadiansValue = y;
                } else {
                    advBisectorMultiplesRadians = false;
                    advBisectorMultiplesRadiansValue = (int) Math.round(2 * Math.PI / bisectors.get(1));
                }

                advBisectorMultiplesDegreeValue = toDegrees(bisectors.get(1));
            } else {
                advBisectorMultiples = false;
            }

            advBisectorAngleDegreeValue = toDegrees(bisectors);

            advancedCones = cones;
            updateAdvancedControls();
        }

        conePreview.setCones(cones);
    }

    private void update() {
        // Update cones
        updateCones();

        // Recompute values for inactive elemetns of the advanced panel
        propagateAdvancedValues();

        // Update UI
        if (modeTabbedPane.getSelectedComponent() == simplePanel) {
            updateSimpleControls();
            conePreview.setCones(simpleCones);
        } else {
            updateAdvancedControls();
            conePreview.setCones(advancedCones);
        }
    }

    private void updateCones() {
        simpleCones = new Cones(simpleTheta, simpleHalf, simpleNCones);

        double aperture;

        if (advConeRadians) {
            aperture = 2 * Math.PI / advConeRadiansValue;
        } else {
            aperture = toRadians(advConeDegreeValue);
        }

        List<Double> bisectors;

        if (advBisectorMultiples) {
            double multipleAngle;

            if (advBisectorMultiplesRadians) {
                multipleAngle = 2 * Math.PI / advBisectorMultiplesRadiansValue;
            } else {
                multipleAngle = toRadians(advBisectorMultiplesDegreeValue);
            }

            bisectors = new ArrayList<Double>();
            for (double alpha = 0; alpha < 2 * Math.PI; alpha += multipleAngle) {
                if (!equal(alpha, 2 * Math.PI)) {
                    bisectors.add(alpha);
                }
            }
        } else {
            // Specific angle values
            bisectors = toRadians(advBisectorAngleDegreeValue);
        }

        Cone[] cones = new Cone[bisectors.size()];

        for (int i = 0; i < bisectors.size(); i++) {
            cones[i] = new Cone(aperture, bisectors.get(i));
        }

        advancedCones = new Cones(advTheta, cones);
    }

    private void propagateAdvancedValues() {
        // Cone angle
        if (advConeRadians) {
            advConeDegreeValue = toDegrees(2 * Math.PI / advConeRadiansValue);
        } else {
            advConeRadiansValue = Math.max(3, (int) Math.round(360 / advConeDegreeValue));
        }

        // Bisectors
        if (advBisectorMultiples) {
            if (advBisectorMultiplesRadians) {
                advBisectorMultiplesDegreeValue = toDegrees(2 * Math.PI / advBisectorMultiplesRadiansValue);
            } else {
                advBisectorMultiplesRadiansValue = (int) Math.round(360 / advBisectorMultiplesDegreeValue);
            }

            advBisectorAngleDegreeValue.clear();

            for (double alpha = 0; alpha < 360; alpha += advBisectorMultiplesDegreeValue) {
                if (!equal(alpha, 360)) {
                    advBisectorAngleDegreeValue.add(alpha);
                }
            }
        } else {
            // Don't update multiples, as they can't even approximate the values
        }
    }

    private void updateSimpleControls() {
        updatingUI = true;

        // Theta / Yao
        if (simpleTheta) {
            thetaToggleButton.setSelected(true);
            yaoToggleButton.setSelected(false);
        } else {
            thetaToggleButton.setSelected(false);
            yaoToggleButton.setSelected(true);
        }

        // Cones
        if (simpleHalf) {
            fullToggleButton.setSelected(false);
            halfToggleButton.setSelected(true);
        } else {
            fullToggleButton.setSelected(true);
            halfToggleButton.setSelected(false);
        }

        conesSpinner.setValue(simpleNCones);

        updatingUI = false;
    }

    private void updateAdvancedControls() {
        updatingUI = true;

        // Type
        advThetaToggleButton.setSelected(advTheta);
        advYaoToggleButton.setSelected(!advTheta);

        // Cone angle
        radiansConeRadioButton.setSelected(advConeRadians);
        radiansConeSpinner.setEnabled(advConeRadians);
        degreeConeRadioButton.setSelected(!advConeRadians);
        degreeConeTextField.setEnabled(!advConeRadians);

        radiansConeSpinner.setValue(advConeRadiansValue);

        if (!editingDegreeCones) {
            degreeConeTextField.setText(printDouble(advConeDegreeValue));
        }

        // Bisectors
        if (advBisectorMultiples) {
            multipleBisectorRadioButton.setSelected(true);
            angleBisectorRadioButton.setSelected(false);

            radiansMultipleBisectorRadioButton.setEnabled(true);
            degreeMultipleBisectorRadioButton.setEnabled(true);
            degreeAngleBisectorTextField.setEnabled(false);

            radiansMultipleBisectorRadioButton.setSelected(advBisectorMultiplesRadians);
            radiansMultipleBisectorSpinner.setEnabled(advBisectorMultiplesRadians);
            degreeMultipleBisectorRadioButton.setSelected(!advBisectorMultiplesRadians);
            degreeMultipleBisectorTextField.setEnabled(!advBisectorMultiplesRadians);
        } else {
            multipleBisectorRadioButton.setSelected(false);
            angleBisectorRadioButton.setSelected(true);

            degreeAngleBisectorTextField.setEnabled(true);

            // Disable all multiple elements
            radiansMultipleBisectorRadioButton.setEnabled(false);
            radiansMultipleBisectorSpinner.setEnabled(false);
            degreeMultipleBisectorRadioButton.setEnabled(false);
            degreeMultipleBisectorTextField.setEnabled(false);
        }

        if (!editingAngle) {
            degreeAngleBisectorTextField.setText(toCSVString(advBisectorAngleDegreeValue));
        }

        radiansMultipleBisectorSpinner.setValue(advBisectorMultiplesRadiansValue);

        if (!editingDegreeMultipleBisectors) {
            degreeMultipleBisectorTextField.setText(printDouble(advBisectorMultiplesDegreeValue));
        }

        updatingUI = false;
    }

    /**
     * Computes the integer x such that alpha = 2 * Math.PI / x, or -1 if no
     * such integer exists.
     *
     * @param alpha
     * @return
     */
    private int twoPiBy(double alpha) {
        double x = 2 * Math.PI / alpha;

        if (equal(x, Math.round(x))) {
            return (int) Math.round(x);
        } else {
            return -1;
        }
    }

    /**
     * Converts the given angle in radians to degrees.
     *
     * @param theta
     * @return
     */
    private double toDegrees(double theta) {
        double degrees = theta * 180 / Math.PI;

        if (equal(degrees, Math.round(degrees))) {
            return Math.round(degrees);
        } else {
            return degrees;
        }
    }

    /**
     * Converts the given list of angles in radians to degrees.
     *
     * @param thetas
     * @return
     */
    private List<Double> toDegrees(List<Double> thetas) {
        List<Double> result = new ArrayList<Double>(thetas.size());

        for (Double t : thetas) {
            result.add(toDegrees(t));
        }

        return result;
    }

    /**
     * Converts the given angle in degrees to radians.
     *
     * @param theta
     * @return
     */
    private double toRadians(double theta) {
        return theta * Math.PI / 180;
    }

    /**
     * Converts the given list of angles in degrees to radians.
     *
     * @param thetas
     * @return
     */
    private List<Double> toRadians(List<Double> thetas) {
        List<Double> result = new ArrayList<Double>(thetas.size());

        for (Double t : thetas) {
            result.add(toRadians(t));
        }

        return result;
    }

    /**
     * Checks if the listed bisectors are all multiples of a given value,
     * starting at 0 and running through 2 * Math.PI.
     *
     * @param bisectors
     * @return
     */
    private boolean areMultiples(List<Double> bisectors) {
        if (bisectors.size() < 2 || !equal(bisectors.get(0), 0)) {
            return false;
        }

        double interval = bisectors.get(1);

        if (!equal(2 * Math.PI / interval, bisectors.size())) {
            return false;
        }

        for (int i = 2; i < bisectors.size(); i++) {
            if (!equal(interval, bisectors.get(i) - bisectors.get(i - 1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts the given list to a String where the values are comma-separated.
     *
     * @param list
     * @return
     */
    private String toCSVString(List<Double> list) {
        StringBuilder result = new StringBuilder(list.size() * 3);

        boolean first = true;

        for (Double d : list) {
            if (first) {
                first = false;
            } else {
                result.append(",");
            }

            result.append(printDouble(d));
        }

        return result.toString();
    }

    /**
     * Converts the given double into a String. If the double value is very
     * close to an integer, the trailing zeroes are omitted.
     *
     * @param d
     * @return
     */
    private String printDouble(double d) {
        if (equal(d, Math.round(d))) {
            return Integer.toString((int) Math.round(d));
        } else {
            return Double.toString(d);
        }
    }

    /**
     * Checks two
     * <code>double</code> values for near-equality.
     *
     * @param a param b
     * @return
     */
    private boolean equal(double a, double b) {
        double EPSILON = 0.0000001;
        return Math.abs(a - b) < EPSILON;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coneMeasure = new javax.swing.ButtonGroup();
        bisectorMode = new javax.swing.ButtonGroup();
        multipleBisectorMeasure = new javax.swing.ButtonGroup();
        modeTabbedPane = new javax.swing.JTabbedPane();
        simplePanel = new javax.swing.JPanel();
        simpleTypePanel = new javax.swing.JPanel();
        thetaToggleButton = new javax.swing.JToggleButton();
        yaoToggleButton = new javax.swing.JToggleButton();
        simpleConesPanel = new javax.swing.JPanel();
        fullToggleButton = new javax.swing.JToggleButton();
        halfToggleButton = new javax.swing.JToggleButton();
        conesSpinner = new javax.swing.JSpinner();
        advancedPanel = new javax.swing.JPanel();
        typePanel = new javax.swing.JPanel();
        advThetaToggleButton = new javax.swing.JToggleButton();
        advYaoToggleButton = new javax.swing.JToggleButton();
        conePanel = new javax.swing.JPanel();
        radiansConeRadioButton = new javax.swing.JRadioButton();
        degreeConeRadioButton = new javax.swing.JRadioButton();
        degreeConeTextField = new javax.swing.JTextField();
        radiansConeSpinner = new javax.swing.JSpinner();
        radiansConeLabel = new javax.swing.JLabel();
        degreeConeLabel = new javax.swing.JLabel();
        bisectorPanel = new javax.swing.JPanel();
        degreeAngleBisectorTextField = new javax.swing.JTextField();
        angleBisectorRadioButton = new javax.swing.JRadioButton();
        degreeMultipleBisectorLabel = new javax.swing.JLabel();
        degreeAngleBisectorLabel = new javax.swing.JLabel();
        multipleBisectorRadioButton = new javax.swing.JRadioButton();
        degreeMultipleBisectorRadioButton = new javax.swing.JRadioButton();
        degreeMultipleBisectorTextField = new javax.swing.JTextField();
        radiansMultipleBisectorSpinner = new javax.swing.JSpinner();
        radiansMultipleBisectorLabel = new javax.swing.JLabel();
        radiansMultipleBisectorRadioButton = new javax.swing.JRadioButton();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        previewPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cones");
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        modeTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                modeTabbedPaneStateChanged(evt);
            }
        });

        simpleTypePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Type"));

        thetaToggleButton.setSelected(true);
        thetaToggleButton.setText("Theta");
        thetaToggleButton.setMaximumSize(new java.awt.Dimension(75, 25));
        thetaToggleButton.setMinimumSize(new java.awt.Dimension(75, 25));
        thetaToggleButton.setPreferredSize(new java.awt.Dimension(75, 25));
        thetaToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thetaToggleButtonActionPerformed(evt);
            }
        });

        yaoToggleButton.setText("Yao");
        yaoToggleButton.setMaximumSize(new java.awt.Dimension(75, 25));
        yaoToggleButton.setMinimumSize(new java.awt.Dimension(75, 25));
        yaoToggleButton.setPreferredSize(new java.awt.Dimension(75, 25));
        yaoToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yaoToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout simpleTypePanelLayout = new javax.swing.GroupLayout(simpleTypePanel);
        simpleTypePanel.setLayout(simpleTypePanelLayout);
        simpleTypePanelLayout.setHorizontalGroup(
            simpleTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(simpleTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(thetaToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yaoToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        simpleTypePanelLayout.setVerticalGroup(
            simpleTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(simpleTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(thetaToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(yaoToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        simpleConesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Cones"));

        fullToggleButton.setText("Full");
        fullToggleButton.setMaximumSize(new java.awt.Dimension(75, 25));
        fullToggleButton.setMinimumSize(new java.awt.Dimension(75, 25));
        fullToggleButton.setPreferredSize(new java.awt.Dimension(75, 25));
        fullToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullToggleButtonActionPerformed(evt);
            }
        });

        halfToggleButton.setText("Half");
        halfToggleButton.setMaximumSize(new java.awt.Dimension(75, 25));
        halfToggleButton.setMinimumSize(new java.awt.Dimension(75, 25));
        halfToggleButton.setPreferredSize(new java.awt.Dimension(75, 25));
        halfToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                halfToggleButtonActionPerformed(evt);
            }
        });

        conesSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(6), Integer.valueOf(3), null, Integer.valueOf(1)));
        conesSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                conesSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout simpleConesPanelLayout = new javax.swing.GroupLayout(simpleConesPanel);
        simpleConesPanel.setLayout(simpleConesPanelLayout);
        simpleConesPanelLayout.setHorizontalGroup(
            simpleConesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(simpleConesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(simpleConesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(conesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(simpleConesPanelLayout.createSequentialGroup()
                        .addComponent(fullToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(halfToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(133, Short.MAX_VALUE))
        );

        simpleConesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {fullToggleButton, halfToggleButton});

        simpleConesPanelLayout.setVerticalGroup(
            simpleConesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(simpleConesPanelLayout.createSequentialGroup()
                .addComponent(conesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(simpleConesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fullToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(halfToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        simpleConesPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fullToggleButton, halfToggleButton});

        javax.swing.GroupLayout simplePanelLayout = new javax.swing.GroupLayout(simplePanel);
        simplePanel.setLayout(simplePanelLayout);
        simplePanelLayout.setHorizontalGroup(
            simplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(simplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(simplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(simpleConesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(simpleTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        simplePanelLayout.setVerticalGroup(
            simplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, simplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(simpleTypePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(simpleConesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(163, Short.MAX_VALUE))
        );

        modeTabbedPane.addTab("Simple", simplePanel);

        typePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Type"));

        advThetaToggleButton.setSelected(true);
        advThetaToggleButton.setText("Theta");
        advThetaToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advThetaToggleButtonActionPerformed(evt);
            }
        });

        advYaoToggleButton.setText("Yao");
        advYaoToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advYaoToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout typePanelLayout = new javax.swing.GroupLayout(typePanel);
        typePanel.setLayout(typePanelLayout);
        typePanelLayout.setHorizontalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(advThetaToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(advYaoToggleButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        typePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {advThetaToggleButton, advYaoToggleButton});

        typePanelLayout.setVerticalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(advThetaToggleButton)
                .addComponent(advYaoToggleButton))
        );

        conePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Cone angle"));

        coneMeasure.add(radiansConeRadioButton);
        radiansConeRadioButton.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        radiansConeRadioButton.setSelected(true);
        radiansConeRadioButton.setText("2π /");
        radiansConeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radiansConeRadioButtonActionPerformed(evt);
            }
        });

        coneMeasure.add(degreeConeRadioButton);
        degreeConeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                degreeConeRadioButtonActionPerformed(evt);
            }
        });

        degreeConeTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateDegreeConeText();
            }
            public void removeUpdate(DocumentEvent e) {
                validateDegreeConeText();
            }
            public void changedUpdate(DocumentEvent e) {
                //Plain text components do not fire these events
            }
        });

        radiansConeSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(6), Integer.valueOf(3), null, Integer.valueOf(1)));
        radiansConeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                radiansConeSpinnerStateChanged(evt);
            }
        });

        radiansConeLabel.setText("Radians");

        degreeConeLabel.setText("Degrees");

        javax.swing.GroupLayout conePanelLayout = new javax.swing.GroupLayout(conePanel);
        conePanel.setLayout(conePanelLayout);
        conePanelLayout.setHorizontalGroup(
            conePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(conePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(conePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(conePanelLayout.createSequentialGroup()
                        .addComponent(degreeConeRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(degreeConeTextField))
                    .addGroup(conePanelLayout.createSequentialGroup()
                        .addComponent(radiansConeRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radiansConeSpinner)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(conePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(degreeConeLabel)
                    .addComponent(radiansConeLabel))
                .addContainerGap())
        );
        conePanelLayout.setVerticalGroup(
            conePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(conePanelLayout.createSequentialGroup()
                .addGroup(conePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radiansConeRadioButton)
                    .addComponent(radiansConeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radiansConeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(conePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(degreeConeRadioButton)
                    .addGroup(conePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(degreeConeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(degreeConeLabel))))
        );

        bisectorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bisectors"));

        degreeAngleBisectorTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateAngleText();
            }
            public void removeUpdate(DocumentEvent e) {
                validateAngleText();
            }
            public void changedUpdate(DocumentEvent e) {
                //Plain text components do not fire these events
            }
        });

        bisectorMode.add(angleBisectorRadioButton);
        angleBisectorRadioButton.setText("These angles (comma-separated)");
        angleBisectorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                angleBisectorRadioButtonActionPerformed(evt);
            }
        });

        degreeMultipleBisectorLabel.setText("Degrees");

        degreeAngleBisectorLabel.setText("Degrees");

        bisectorMode.add(multipleBisectorRadioButton);
        multipleBisectorRadioButton.setSelected(true);
        multipleBisectorRadioButton.setText("All multiples of");
        multipleBisectorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multipleBisectorRadioButtonActionPerformed(evt);
            }
        });

        multipleBisectorMeasure.add(degreeMultipleBisectorRadioButton);
        degreeMultipleBisectorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                degreeMultipleBisectorRadioButtonActionPerformed(evt);
            }
        });

        degreeMultipleBisectorTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateDegreeMultipleBisectorText();
            }
            public void removeUpdate(DocumentEvent e) {
                validateDegreeMultipleBisectorText();
            }
            public void changedUpdate(DocumentEvent e) {
                //Plain text components do not fire these events
            }
        });

        radiansMultipleBisectorSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(6), Integer.valueOf(2), null, Integer.valueOf(1)));
        radiansMultipleBisectorSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                radiansMultipleBisectorSpinnerStateChanged(evt);
            }
        });

        radiansMultipleBisectorLabel.setText("Radians");

        multipleBisectorMeasure.add(radiansMultipleBisectorRadioButton);
        radiansMultipleBisectorRadioButton.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        radiansMultipleBisectorRadioButton.setSelected(true);
        radiansMultipleBisectorRadioButton.setText("2π /");
        radiansMultipleBisectorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radiansMultipleBisectorRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bisectorPanelLayout = new javax.swing.GroupLayout(bisectorPanel);
        bisectorPanel.setLayout(bisectorPanelLayout);
        bisectorPanelLayout.setHorizontalGroup(
            bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bisectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bisectorPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(bisectorPanelLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(degreeAngleBisectorTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(degreeAngleBisectorLabel))
                            .addGroup(bisectorPanelLayout.createSequentialGroup()
                                .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(bisectorPanelLayout.createSequentialGroup()
                                        .addComponent(degreeMultipleBisectorRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(degreeMultipleBisectorTextField))
                                    .addGroup(bisectorPanelLayout.createSequentialGroup()
                                        .addComponent(radiansMultipleBisectorRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(radiansMultipleBisectorSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(degreeMultipleBisectorLabel)
                                    .addComponent(radiansMultipleBisectorLabel)))))
                    .addComponent(multipleBisectorRadioButton)
                    .addComponent(angleBisectorRadioButton))
                .addContainerGap())
        );
        bisectorPanelLayout.setVerticalGroup(
            bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bisectorPanelLayout.createSequentialGroup()
                .addComponent(multipleBisectorRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radiansMultipleBisectorRadioButton)
                    .addComponent(radiansMultipleBisectorSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radiansMultipleBisectorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(degreeMultipleBisectorRadioButton)
                    .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(degreeMultipleBisectorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(degreeMultipleBisectorLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(angleBisectorRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bisectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(degreeAngleBisectorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(degreeAngleBisectorLabel))
                .addContainerGap())
        );

        javax.swing.GroupLayout advancedPanelLayout = new javax.swing.GroupLayout(advancedPanel);
        advancedPanel.setLayout(advancedPanelLayout);
        advancedPanelLayout.setHorizontalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(conePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bisectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        advancedPanelLayout.setVerticalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(typePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bisectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        modeTabbedPane.addTab("Advanced", advancedPanel);

        getContentPane().add(modeTabbedPane, java.awt.BorderLayout.NORTH);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap(143, Short.MAX_VALUE)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyButton)
                .addContainerGap())
        );

        buttonPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {applyButton, cancelButton, okButton});

        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(applyButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Preview"));

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 330, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 148, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout centerPanelLayout = new javax.swing.GroupLayout(centerPanel);
        centerPanel.setLayout(centerPanelLayout);
        centerPanelLayout.setHorizontalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        centerPanelLayout.setVerticalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (modeTabbedPane.getSelectedComponent() == simplePanel) {
            drawPanel.setCones(simpleCones);
        } else {
            drawPanel.setCones(advancedCones);
        }
        drawPanel.repaint();
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            okButtonActionPerformed(null);
        }
    }//GEN-LAST:event_formKeyReleased

    private void conesSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_conesSpinnerStateChanged
        if (!updatingUI) {
            simpleNCones = (Integer) conesSpinner.getValue();
            update();
        }
    }//GEN-LAST:event_conesSpinnerStateChanged

    private void thetaToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thetaToggleButtonActionPerformed
        if (!updatingUI) {
            simpleTheta = true;
            update();
        }
    }//GEN-LAST:event_thetaToggleButtonActionPerformed

    private void yaoToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yaoToggleButtonActionPerformed
        if (!updatingUI) {
            simpleTheta = false;
            update();
        }
    }//GEN-LAST:event_yaoToggleButtonActionPerformed

    private void advThetaToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advThetaToggleButtonActionPerformed
        if (!updatingUI) {
            advTheta = true;
            update();
        }
    }//GEN-LAST:event_advThetaToggleButtonActionPerformed

    private void advYaoToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advYaoToggleButtonActionPerformed
        if (!updatingUI) {
            advTheta = false;
            update();
        }
    }//GEN-LAST:event_advYaoToggleButtonActionPerformed

    private void modeTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_modeTabbedPaneStateChanged
        if (conePreview != null) {
            if (modeTabbedPane.getSelectedComponent() == simplePanel) {
                conePreview.setCones(simpleCones);
            } else {
                conePreview.setCones(advancedCones);
            }
        }
    }//GEN-LAST:event_modeTabbedPaneStateChanged

    private void fullToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullToggleButtonActionPerformed
        if (!updatingUI) {
            simpleHalf = false;
            update();
        }
    }//GEN-LAST:event_fullToggleButtonActionPerformed

    private void halfToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_halfToggleButtonActionPerformed
        if (!updatingUI) {
            simpleHalf = true;
            update();
        }
    }//GEN-LAST:event_halfToggleButtonActionPerformed

    private void radiansConeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radiansConeRadioButtonActionPerformed
        if (!updatingUI) {
            advConeRadians = true;
            update();
        }
    }//GEN-LAST:event_radiansConeRadioButtonActionPerformed

    private void degreeConeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_degreeConeRadioButtonActionPerformed
        if (!updatingUI) {
            advConeRadians = false;
            update();
        }
    }//GEN-LAST:event_degreeConeRadioButtonActionPerformed

    private void multipleBisectorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multipleBisectorRadioButtonActionPerformed
        if (!updatingUI) {
            advBisectorMultiples = true;
            update();
        }
    }//GEN-LAST:event_multipleBisectorRadioButtonActionPerformed

    private void radiansMultipleBisectorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radiansMultipleBisectorRadioButtonActionPerformed
        if (!updatingUI) {
            advBisectorMultiplesRadians = true;
            update();
        }
    }//GEN-LAST:event_radiansMultipleBisectorRadioButtonActionPerformed

    private void degreeMultipleBisectorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_degreeMultipleBisectorRadioButtonActionPerformed
        if (!updatingUI) {
            advBisectorMultiplesRadians = false;
            update();
        }
    }//GEN-LAST:event_degreeMultipleBisectorRadioButtonActionPerformed

    private void angleBisectorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_angleBisectorRadioButtonActionPerformed
        if (!updatingUI) {
            advBisectorMultiples = false;
            update();
        }
    }//GEN-LAST:event_angleBisectorRadioButtonActionPerformed

    private void radiansConeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_radiansConeSpinnerStateChanged
        if (!updatingUI) {
            advConeRadiansValue = (Integer) radiansConeSpinner.getValue();
            update();
        }
    }//GEN-LAST:event_radiansConeSpinnerStateChanged

    private void radiansMultipleBisectorSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_radiansMultipleBisectorSpinnerStateChanged
        if (!updatingUI) {
            advBisectorMultiplesRadiansValue = (Integer) radiansMultipleBisectorSpinner.getValue();
            update();
        }
    }//GEN-LAST:event_radiansMultipleBisectorSpinnerStateChanged

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        if (modeTabbedPane.getSelectedComponent() == simplePanel) {
            drawPanel.setCones(simpleCones);
        } else {
            drawPanel.setCones(advancedCones);
        }
        drawPanel.repaint();
    }//GEN-LAST:event_applyButtonActionPerformed
    private static final Color correctBackground = Color.white;
    private static final Color incorrectBackground = new Color(255, 158, 158);

    private void validateDegreeConeText() {
        if (!updatingUI) {
            try {
                double val = Double.parseDouble(degreeConeTextField.getText().trim());

                if (val <= 0 || val >= 180) {
                    throw new NumberFormatException();
                }

                advConeDegreeValue = val;
                degreeConeTextField.setBackground(correctBackground);

                editingDegreeCones = true;
                update();
                editingDegreeCones = false;
            } catch (NumberFormatException ex) {
                degreeConeTextField.setBackground(incorrectBackground);
            }
        }
    }

    private void validateDegreeMultipleBisectorText() {
        if (!updatingUI) {
            try {
                double val = Double.parseDouble(degreeMultipleBisectorTextField.getText().trim());

                if (val <= 0 || val >= 360) {
                    throw new NumberFormatException();
                }

                advBisectorMultiplesDegreeValue = val;
                degreeMultipleBisectorTextField.setBackground(correctBackground);

                editingDegreeMultipleBisectors = true;
                update();
                editingDegreeMultipleBisectors = false;
            } catch (NumberFormatException ex) {
                degreeMultipleBisectorTextField.setBackground(incorrectBackground);
            }
        }
    }

    private void validateAngleText() {
        if (!updatingUI) {
            try {
                String[] parts = degreeAngleBisectorTextField.getText().split(",");
                List<Double> vals = new ArrayList<Double>(parts.length);

                for (String part : parts) {
                    double val = Double.parseDouble(part.trim());

                    if (val < 0 || val >= 360) {
                        throw new NumberFormatException();
                    }

                    vals.add(val);
                }

                advBisectorAngleDegreeValue = vals;
                degreeAngleBisectorTextField.setBackground(correctBackground);

                editingAngle = true;
                update();
                editingAngle = false;
            } catch (NumberFormatException ex) {
                degreeAngleBisectorTextField.setBackground(incorrectBackground);
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton advThetaToggleButton;
    private javax.swing.JToggleButton advYaoToggleButton;
    private javax.swing.JPanel advancedPanel;
    private javax.swing.JRadioButton angleBisectorRadioButton;
    private javax.swing.JButton applyButton;
    private javax.swing.ButtonGroup bisectorMode;
    private javax.swing.JPanel bisectorPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel centerPanel;
    private javax.swing.ButtonGroup coneMeasure;
    private javax.swing.JPanel conePanel;
    private javax.swing.JSpinner conesSpinner;
    private javax.swing.JLabel degreeAngleBisectorLabel;
    private javax.swing.JTextField degreeAngleBisectorTextField;
    private javax.swing.JLabel degreeConeLabel;
    private javax.swing.JRadioButton degreeConeRadioButton;
    private javax.swing.JTextField degreeConeTextField;
    private javax.swing.JLabel degreeMultipleBisectorLabel;
    private javax.swing.JRadioButton degreeMultipleBisectorRadioButton;
    private javax.swing.JTextField degreeMultipleBisectorTextField;
    private javax.swing.JToggleButton fullToggleButton;
    private javax.swing.JToggleButton halfToggleButton;
    private javax.swing.JTabbedPane modeTabbedPane;
    private javax.swing.ButtonGroup multipleBisectorMeasure;
    private javax.swing.JRadioButton multipleBisectorRadioButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel radiansConeLabel;
    private javax.swing.JRadioButton radiansConeRadioButton;
    private javax.swing.JSpinner radiansConeSpinner;
    private javax.swing.JLabel radiansMultipleBisectorLabel;
    private javax.swing.JRadioButton radiansMultipleBisectorRadioButton;
    private javax.swing.JSpinner radiansMultipleBisectorSpinner;
    private javax.swing.JPanel simpleConesPanel;
    private javax.swing.JPanel simplePanel;
    private javax.swing.JPanel simpleTypePanel;
    private javax.swing.JToggleButton thetaToggleButton;
    private javax.swing.JPanel typePanel;
    private javax.swing.JToggleButton yaoToggleButton;
    // End of variables declaration//GEN-END:variables
}
