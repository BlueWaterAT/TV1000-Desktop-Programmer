package com.bwat.programmer;

import javax.swing.JFrame;

/**
 * Driver for the TV1000 Programmer
 *
 * @author Kareem ElFaramawi
 */
public class TV1000Programmer {
    public static void main(String[] args) {
        JFrame frame = new JFrame("TV1000");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Programmer prog = new Programmer();
        frame.add(prog);
        frame.pack();
        frame.setVisible(true);
    }
}
