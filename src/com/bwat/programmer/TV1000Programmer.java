package com.bwat.programmer;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;

/**
 * Driver for the TV1000 Programmer
 *
 * @author Kareem ElFaramawi
 */
public class TV1000Programmer {
    public static void main(String[] args) {
        // UI Fixes
        UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
        UIManager.put("CheckBox.focus", new ColorUIResource(new Color(0, 0, 0, 0)));

        // Display main window
        JFrame frame = new JFrame("TV1000");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Programmer prog = new Programmer();
        frame.add(prog);
        frame.pack();
        frame.setVisible(true);
    }
}
