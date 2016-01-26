package com.bwat.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JPanel;

public class SwingUtils {
	public static void setFont_r( Component c, Font font ) {
		c.setFont( font );
		if ( c instanceof Container ) {
			for ( Component comp : ( (Container) c ).getComponents() ) {
				setFont_r( comp, font );
			}
		}
	}
	
	public static JPanel createGridJPanel( int rows, int cols, Component... comps ) {
		JPanel panel = new JPanel( new GridLayout( rows, cols ) );
		for ( Component c : comps ) {
			panel.add( c );
		}
		return panel;
	}
}
