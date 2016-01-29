package com.bwat.util;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Some convenient methods for working with Swing
 *
 * @author Kareem El-Faramawi
 */
public class SwingUtils {

    /**
     * Recursively sets the Font of a component and all of its inner components
     *
     * @param c    Root component
     * @param font The new Font
     */
    public static void setFont_r(Component c, Font font) {
        c.setFont(font);
        if (c instanceof Container) {
            for (Component comp : ((Container) c).getComponents()) {
                setFont_r(comp, font);
            }
        }
    }

    /**
     * Creates a JPanel with a GridLayout and fills it with components
     *
     * @param rows  The number of rows in the grid
     * @param cols  The number of columns in the grid
     * @param comps Components to fill the grid with
     * @return The newly filled JPanel
     */
    public static JPanel createGridJPanel(int rows, int cols, Component... comps) {
        JPanel panel = new JPanel(new GridLayout(rows, cols));
        for (Component c : comps) {
            panel.add(c);
        }
        return panel;
    }

    /**
     * Creates a JLabel with a certain font size
     *
     * @param message  The JLabel's text
     * @param fontSize Font size for the text
     * @return The generated JLabel
     */
    public static JLabel createJLabel(String message, float fontSize) {
        JLabel label = new JLabel(message);
        label.setFont(label.getFont().deriveFont(fontSize));
        return label;
    }
}
