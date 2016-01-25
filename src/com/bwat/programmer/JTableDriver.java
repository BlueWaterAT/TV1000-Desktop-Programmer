package com.bwat.programmer;

import javax.swing.JFrame;

public class JTableDriver {
	public static void main( String[] args ) {
		JFrame frame = new JFrame( "TV1000" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		InteractiveJTable t = new InteractiveJTable();
		frame.add( t );
		frame.pack();
		frame.setVisible( true );
	}
}
