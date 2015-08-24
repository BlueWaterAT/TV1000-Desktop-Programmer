package com.bwat.programmer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class ParameterEditor extends JFrame {
	private JButton cancel = new JButton( "Close" );
	private JButton saveExit = new JButton( "Save & Close" );
	private JTable parameters = new JTable( 100, 2 );
	private String path;
	public static final String PARAM_EXTENSION = ".prm";
	
	public ParameterEditor( int size ) {
		JPanel mainPanel = new JPanel( new BorderLayout() );
		
		// Control buttons
		cancel.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				close();
			}
		} );
		saveExit.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				saveParameters();
				close();
			}
		} );
		JPanel controls = SwingUtils.createGridJPanel( 1, 2, cancel, saveExit );
		SwingUtils.setFont_r( controls, controls.getFont().deriveFont( 18f ).deriveFont( Font.BOLD ) );
		mainPanel.add( controls, BorderLayout.SOUTH );
		
		// Editor
		JPanel editorPanel = new JPanel( new BorderLayout() );
		if ( size > 0 ) {
			( (DefaultTableModel) parameters.getModel() ).setRowCount( size );
		}
		( (DefaultTableModel) parameters.getModel() ).setColumnIdentifiers( new String[] { "Data", "Comment" } );
		parameters.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		parameters.getColumnModel().getColumn( 0 ).setMaxWidth( 100 );
		
		JScrollPane scroll = new JScrollPane( parameters );
		scroll.setRowHeaderView( new RowNumberHeader( parameters ) );
		editorPanel.add( scroll, BorderLayout.CENTER );
		mainPanel.add( editorPanel, BorderLayout.CENTER );
		
		mainPanel.setPreferredSize( new Dimension( 600, 600 ) );
		add( mainPanel, BorderLayout.CENTER );
		setTitle( "Parameter Editor" );
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent arg0 ) {
				if ( JOptionPane.showConfirmDialog( null, "Save Parameters?", "Unsaved Data", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ) {
					saveParameters();
				}
			};
		} );
		pack();
	}
	
	public void close() {
		setVisible( false );
		dispose();
	}
	
	public void clearAll() {
		for ( int row = 0; row < parameters.getRowCount(); row++ ) {
			for ( int col = 0; col < parameters.getColumnCount(); col++ ) {
				parameters.setValueAt( null, row, col );
			}
		}
	}
	
	public void setPath( String path ) {
		this.path = path;
	}
	
	public void loadParameters() {
		if ( path != null && path.length() > 0 ) {
			File file = new File( path );
			if ( file.exists() ) {
				clearAll();
				try {
					Scanner scan = new Scanner( file );
					String line;
					while ( scan.hasNext() ) {
						line = scan.nextLine();
						if ( line.length() > 0 ) {
							String[] csv = line.split( "," );
							int row = Integer.parseInt( csv[0] );
							Object data = csv[1].equals( "null" ) ? null : csv[1];
							Object comment = csv[2].equals( "null" ) ? null : csv[2];
							parameters.setValueAt( data, row, 0 );
							parameters.setValueAt( comment, row, 1 );
						}
					}
					scan.close();
				} catch ( FileNotFoundException e ) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void saveParameters() {
		if ( path != null && path.length() > 0 ) {
			try {
				PrintWriter pw = new PrintWriter( new FileOutputStream( new File( path ) ) );
				for ( int row = 0, rows = parameters.getRowCount(); row < rows; row++ ) {
					Object data = parameters.getValueAt( row, 0 );
					Object comment = parameters.getValueAt( row, 1 );
					if ( data != null || comment != null ) {
						pw.println( row + "," + data + "," + comment );
					}
				}
				pw.flush();
				pw.close();
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			}
		}
	}
}
