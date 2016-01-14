package com.bwat.programmer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class InteractiveJTable extends JPanel {
	private Vector<CellType> columnTypes;
	private final JTable table = new JTable( 2, 14 );
	private Vector<Object> rowCopy = null;
	private JFileChooser browser = new JFileChooser();
	private JSpinner indexSelector = new JSpinner( new SpinnerNumberModel( 1, 1, Integer.MAX_VALUE, 1 ) );
	private JButton params = new JButton( "Parameters" );
	private JButton insert = new JButton( "Insert Row" );
	private JButton delete = new JButton( "Delete Row" );
	private JButton copy = new JButton( "Copy Row" );
	private JButton paste = new JButton( "Paste Row" );
	// private JButton save = new JButton( "Save Table" );
	private JButton saveAs = new JButton( "Save As..." );
	private JButton load = new JButton( "Load Table" );
	private JButton send = new JButton( "Send" );
	private JButton download = new JButton( "Download" );
	private final static String EXTENSION = ".jtb";
	private final static String PROGRAM_EXTENSION = ".prg";
	private final static String COMMA = ",";
	private final static String COMMENT = ";";
	private int popupCol = 0;
	private String openFilePath = null;
	private Vector<String> tooltips = new Vector<String>();
	private Vector<Vector<Object>> uneditedData;
	private ParameterEditor paramEditor = new ParameterEditor( 255 );
	public boolean loadingTable = false;
	
	// FTP related variables
	private final static int FTP_PORT = 22;
	private final static String FTP_USER = "root";
	private final static String FTP_PASS = "bwat1234";
	private final static String FTP_REMOTE_DIR = "/hmi/prg/";
	
	private void runSFTPCommand(final String host, final SFTPAction action, final String progressMsg, final String success) {
		if ( host != null & NetUtils.isValidIPAddress( host ) ) {
			// Display a loading dialog
			final JDialog sending = new JDialog();
			sending.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
			sending.setModalityType( JDialog.DEFAULT_MODALITY_TYPE );
			
			JProgressBar progress = new JProgressBar();
			progress.setIndeterminate( true );
			
			sending.add( SwingUtils.createGridJPanel( 2, 1, new JLabel( progressMsg ), progress ) );
			sending.pack();
			sending.setLocationRelativeTo( null );
			
			new Thread() {
				public void run() {
					// Save the table
					saveTableToPath( openFilePath );
					
					// Get the file for sending
					// File tableFile = new File( openFilePath );
					String msg = "";
					try {
						SSHClient ssh = new SSHClient();
						ssh.addHostKeyVerifier( new PromiscuousVerifier() );
						ssh.connect( host, FTP_PORT );
						ssh.authPassword( FTP_USER, FTP_PASS );
						SFTPClient sftp = ssh.newSFTPClient();
						
						//Run the SFTP action
						action.run( sftp );
						
						sftp.close();
						ssh.disconnect();
						
						msg = success;
					} catch ( final IOException e ) {
						msg = " SFTP Error: " + e.getMessage();
					} finally {
						sending.setVisible( false );
						JOptionPane.showMessageDialog( InteractiveJTable.this, msg );
					}
				}
			}.start();
			
			sending.setVisible( true );
			
		}
	}
	
	private void downloadFile( final String host ) {
		runSFTPCommand( host, new SFTPAction() {
			@Override
			public void run( SFTPClient sftp ) throws IOException {
				// Right now this only downloads the PRG file
				// Code for downloading the other two is commented here in case it's needed later
				
//				File jtb = new File( getTablePath() ), prg = new File( getProgramPath( (int) indexSelector.getValue() ) ), prm = new File( getParamPath() );
				File prg = new File( getProgramPath( (int) indexSelector.getValue() ) );
				
				// Download the JTB file
				// String remote = FTP_REMOTE_DIR + jtb.getName();
				// sftp.put( jtb.getPath(), remote );
				// sftp.get( remote, jtb.getPath() );
				
				// Download the PRG file
				String remote = FTP_REMOTE_DIR + prg.getName();
				sftp.get( remote, prg.getPath() );
				
				// Download the PRM file
				// remote = FTP_REMOTE_DIR + prm.getName();
				// sftp.get( remote, prm.getPath() );
				
				loadProgram( 1 );
			}
		}, "Downloading...", "Program download successful!" );
	}
	
	private void sendFile( final String host ) {
		runSFTPCommand( host, new SFTPAction() {
			@Override
			public void run( SFTPClient sftp ) throws IOException {
				File jtb = new File( getTablePath() ), prg = new File( getProgramPath( (int) indexSelector.getValue() ) ), prm = new File( getParamPath() );
				
				// Send the JTB file
				String remote = FTP_REMOTE_DIR + jtb.getName();
				sftp.put( jtb.getPath(), remote );
				
				// Send the PRG file
				remote = FTP_REMOTE_DIR + prg.getName();
				sftp.put( prg.getPath(), remote );
				
				// Send the PRM file
				remote = FTP_REMOTE_DIR + prm.getName();
				sftp.put( prm.getPath(), remote );
			}
		}, "Sending...", "Program upload successful!" );
	}
	
	public InteractiveJTable() {
		table.setModel( new DefaultTableModel( 2, 14 ) {
			@Override
			public Class<?> getColumnClass( int columnIndex ) {
				if ( !loadingTable ) {
					CellType type = columnTypes.get( columnIndex );
					if ( type == CellType.COMBO ) {
						JComboBox<String> combo = (JComboBox<String>) table.getCellRenderer( 0, columnIndex );
						
						// Convert combo entries to an array because this method doesn't exist for some god
						// forsaken reason
						String[] entries = new String[combo.getItemCount()];
						for ( int i = 0; i < combo.getItemCount(); i++ ) {
							entries[i] = combo.getItemAt( i );
						}
						return columnTypes.get( columnIndex ).getCellClass( entries );
					}
					return columnTypes.get( columnIndex ).getCellClass();
				}
				return super.getColumnClass( columnIndex );
			}
		} );
		
		// Intial setup
		setPreferredSize( new Dimension( 950, 800 ) );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.setRowHeight( 25 );
		// File browser options
		browser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
		browser.setFileFilter( new FileFilter() {
			public String getDescription() {
				return "JTable Data";
			}
			
			public boolean accept( File f ) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith( EXTENSION );
			}
		} );
		// Initialize column data types
		columnTypes = new Vector<CellType>( table.getColumnCount() );
		for ( int i = 0; i < columnTypes.capacity(); i++ ) {
			columnTypes.add( null );
			setColumnType( i, CellType.TEXT );
			tooltips.add( "Col " + i );
		}
		
		// CONTROLS
		paste.setEnabled( false );
		// save.setEnabled( false );
		params.setEnabled( false );
		send.setEnabled( false );
		download.setEnabled( false );
		// JPanel controls = new JPanel(new GridLayout( 1, 3 ));
		JPanel controls = new JPanel( new GridLayout( 1, 6 ) );
		controls.setPreferredSize( new Dimension( getWidth(), 150 ) );
		// INDEX SELECTOR
		uneditedData = exportTableData();
		indexSelector.addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent e ) {
				loadProgram( (int) indexSelector.getValue() );
			}
		} );
		// PARAMS
		params.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				paramEditor.setVisible( true );
				if ( openFilePath != null ) {
					String path = openFilePath;
					if ( path.endsWith( EXTENSION ) ) {
						path = openFilePath.substring( 0, openFilePath.lastIndexOf( EXTENSION ) );
					}
					paramEditor.setPath( path + ParameterEditor.PARAM_EXTENSION );
				}
				paramEditor.loadParameters();
			}
		} );
		// INSERT
		insert.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				insertRow();
			}
		} );
		// DELETE
		delete.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				deleteRow( table.getSelectedRow() );
			}
		} );
		// COPY
		copy.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				copyRow( table.getSelectedRow() );
				paste.setEnabled( true );
			}
		} );
		// PASTE
		paste.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				pasteRow( table.getSelectedRow() );
			}
		} );
		// SAVE AS
		saveAs.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				saveTableAs();
			}
		} );
		// LOAD
		load.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				loadTable();
			}
		} );
		// SEND
		send.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				JTextField hostText = new JTextField();
				if ( JOptionPane.showConfirmDialog( null, new Object[] { "Enter vehicle IP address", hostText }, "Send Program", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION ) {
					sendFile( hostText.getText() );
				}
			}
		} );

		// DOWNLOAD
		download.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				JTextField hostText = new JTextField();
				if ( JOptionPane.showConfirmDialog( null, new Object[] { "Enter vehicle IP address", hostText }, "Download Program", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION ) {
					downloadFile( hostText.getText() );
				}
			}
		} );
		// controls.add( SwingUtils.createGridJPanel( 2, 1, indexSelector, ) );
		controls.add( SwingUtils.createGridJPanel( 2, 1, insert, params ) );
		controls.add( SwingUtils.createGridJPanel( 2, 1, delete, saveAs ) );
		controls.add( SwingUtils.createGridJPanel( 2, 1, copy, load ) );
		controls.add( SwingUtils.createGridJPanel( 2, 1, paste, send ) );
		controls.add( download );
		SwingUtils.setFont_r( controls, controls.getFont().deriveFont( 28.0f ).deriveFont( Font.BOLD ) );
		
		// COLUMN HEADER POPUP MENU CONTROLS
		final JPopupMenu headerMenu = new JPopupMenu();
		table.getTableHeader().addMouseListener( new MouseAdapter() {
			public void mouseReleased( MouseEvent e ) {
				if ( SwingUtilities.isRightMouseButton( e ) ) {
					popupCol = table.getTableHeader().columnAtPoint( e.getPoint() );
					headerMenu.show( table.getTableHeader(), e.getX(), e.getY() );
				}
			}
		} );
		// Tooltip display
		table.getTableHeader().addMouseMotionListener( new MouseAdapter() {
			public void mouseMoved( MouseEvent e ) {
				int col = table.getTableHeader().columnAtPoint( e.getPoint() );
				table.getTableHeader().setToolTipText( tooltips.get( col ) );
			}
		} );
		// Popup menu choices
		JMenu jmi_type = new JMenu( "Column Type" );
		// COlUMN DATA TYPES
		JMenuItem jmi_text = new JMenuItem( "Text" );
		JMenuItem jmi_check = new JMenuItem( "Checkbox" );
		JMenuItem jmi_combo = new JMenuItem( "Combo Box" );
		JMenuItem jmi_num = new JMenuItem( "Number" );
		// Text type
		jmi_text.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				setColumnType( popupCol, CellType.TEXT );
				repaint();
			}
		} );
		// Checkbox type
		jmi_check.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				setColumnType( popupCol, CellType.CHECK );
				repaint();
			}
		} );
		// Combo box type
		jmi_combo.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				JSpinner numEntries = new JSpinner();
				if ( JOptionPane.showConfirmDialog( null, new Object[] { "How many entries?", numEntries }, "Combo Box Options", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION ) {
					int ents = (int) numEntries.getValue();
					if ( ents > 0 ) {
						JTextField[] inputs = new JTextField[ents];
						for ( int i = 0; i < ents; i++ ) {
							inputs[i] = new JTextField();
						}
						if ( JOptionPane.showConfirmDialog( null, inputs, "Enter Combo Box Choices", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION ) {
							String[] entries = new String[ents];
							for ( int i = 0; i < ents; i++ ) {
								entries[i] = inputs[i].getText();
							}
							setColumnType( popupCol, CellType.COMBO, entries );
						}
					}
				}
				repaint();
			}
		} );
		// Text type
		jmi_num.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				setColumnType( popupCol, CellType.NUMBER );
				repaint();
			}
		} );
		jmi_type.add( jmi_text );
		jmi_type.add( jmi_check );
		jmi_type.add( jmi_combo );
		jmi_type.add( jmi_num );
		// Option to rename header
		JMenuItem jmi_rename = new JMenuItem( "Rename" );
		jmi_rename.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				JPanel dialog = new JPanel( new GridLayout( 2, 2 ) );
				JTextField name = new JTextField(), tooltip = new JTextField();
				dialog.add( new JLabel( "Name:" ) );
				dialog.add( name );
				dialog.add( new JLabel( "Tooltip:" ) );
				dialog.add( tooltip );
				if ( JOptionPane.showConfirmDialog( null, dialog, "Column Settings", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION ) {
					setColumnHeader( popupCol, name.getText() );
					tooltips.set( popupCol, tooltip.getText() );
				}
				repaint();
			}
		} );
		// Option to add column
		JMenuItem jmi_add = new JMenuItem( "Add Column" );
		final JMenuItem jmi_delete = new JMenuItem( "Delete Column" );
		jmi_add.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				JPanel dialog = new JPanel( new GridLayout( 2, 2 ) );
				JTextField name = new JTextField(), tooltip = new JTextField();
				dialog.add( new JLabel( "Name:" ) );
				dialog.add( name );
				dialog.add( new JLabel( "Tooltip:" ) );
				dialog.add( tooltip );
				if ( JOptionPane.showConfirmDialog( null, dialog, "Column Settings", JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION ) {
					table.getColumnModel().addColumn( new TableColumn() );
					columnTypes.add( null );
					setColumnType( table.getColumnCount() - 1, CellType.TEXT );
					setColumnHeader( table.getColumnCount() - 1, name.getText() );
					tooltips.add( tooltip.getText() );
					if ( !jmi_delete.isEnabled() ) {
						jmi_delete.setEnabled( true );
					}
				}
				repaint();
			}
		} );
		// Option to delete column
		jmi_delete.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				table.getColumnModel().removeColumn( table.getColumnModel().getColumn( popupCol ) );
				columnTypes.remove( popupCol );
				tooltips.remove( popupCol );
				if ( table.getColumnCount() == 1 ) {
					jmi_delete.setEnabled( false );
				}
				repaint();
			}
		} );
		headerMenu.add( jmi_type );
		headerMenu.add( jmi_rename );
		headerMenu.add( jmi_add );
		headerMenu.add( jmi_delete );
		
		// Fix column types when reordering
		table.getColumnModel().addColumnModelListener( new TableColumnModelListener() {
			public void columnMoved( TableColumnModelEvent e ) {
				if ( e.getFromIndex() != e.getToIndex() ) {
					columnTypes.add( e.getToIndex(), columnTypes.remove( e.getFromIndex() ) );
					tooltips.add( e.getToIndex(), tooltips.remove( e.getFromIndex() ) );
					paste.setEnabled( false );
				}
			}
			
			public void columnSelectionChanged( ListSelectionEvent e ) {}
			
			public void columnRemoved( TableColumnModelEvent e ) {}
			
			public void columnMarginChanged( ChangeEvent e ) {}
			
			public void columnAdded( TableColumnModelEvent e ) {}
		} );
		
		// resetSort();
		table.setAutoCreateRowSorter( true );
		DefaultRowSorter sorter = ( (DefaultRowSorter) table.getRowSorter() );
		table.setRowSorter( sorter );
		ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add( new RowSorter.SortKey( 0, SortOrder.ASCENDING ) );
		
		sorter.setSortKeys( sortKeys );
		sorter.setSortsOnUpdates( true );
		
		table.getModel().addTableModelListener( new TableModelListener() {
			@Override
			public void tableChanged( TableModelEvent e ) {
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						if ( uneditedData != null && !uneditedData.equals( exportTableData() ) ) {
							if ( openFilePath != null ) {
								saveTableToPath( openFilePath );
							}
						}
					}
				} );
			}
		} );
		
		setLayout( new BorderLayout() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setRowHeaderView( new RowNumberHeader( table ) );
		add( scroll );
		add( controls, BorderLayout.SOUTH );
	}
	
	// public void resetSort() {
	// table.setAutoCreateRowSorter( true );
	// DefaultRowSorter sorter = ( (DefaultRowSorter) table.getRowSorter() );
	// table.setRowSorter( sorter );
	// ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
	// sortKeys.add( new RowSorter.SortKey( 0, SortOrder.ASCENDING ) );
	// sorter.setSortKeys( sortKeys );
	// sorter.setSortsOnUpdates( true );
	// }
	
	public void loadProgram( int prog ) {
		if ( prog > 0 ) {
			uneditedData = null;
			( (DefaultTableModel) table.getModel() ).setRowCount( 2 );
			clearTable();
			if ( openFilePath != null ) {
				String progPath = openFilePath.substring( 0, openFilePath.endsWith( EXTENSION ) ? openFilePath.lastIndexOf( EXTENSION ) : openFilePath.length() ) + "-" + prog + PROGRAM_EXTENSION;
				if ( new File( progPath ).exists() ) {
					loadTableData( progPath );
				}
			}
			uneditedData = exportTableData();
		}
	}
	
	public void clearTable() {
		for ( int row = 0; row < table.getRowCount(); row++ ) {
			for ( int col = 0; col < table.getColumnCount(); col++ ) {
				table.setValueAt( null, row, col );
			}
		}
	}
	
	public void setData( Vector<Vector<Object>> data ) {
		( (DefaultTableModel) table.getModel() ).setRowCount( data.size() );
		clearTable();
		for ( int row = 0; row < table.getRowCount(); row++ ) {
			for ( int col = 0; col < table.getColumnCount(); col++ ) {
				table.setValueAt( data.get( row ).get( col ), row, col );
			}
		}
	}
	
	public void setColumnHeader( int column, String header ) {
		table.getColumnModel().getColumn( column ).setHeaderValue( header );
	}
	
	public void setColumnHeaders( String... headers ) {
		for ( int i = 0, last = Math.min( headers.length, table.getColumnCount() ); i < last; i++ ) {
			setColumnHeader( i, headers[i] );
		}
	}
	
	/**
	 * Sets a columns to a specific type
	 * 
	 * @param column Column index
	 * @param type Column type
	 * @param comboEntries OPTIONAL, fill only if the type is COMBO, then this is the entries of the that
	 * combo box
	 */
	public void setColumnType( int column, CellType type, String... comboEntries ) {
		if ( column >= 0 && column < table.getColumnCount() ) {
			for ( int i = 0; i < table.getRowCount(); i++ ) {
				table.setValueAt( null, i, column );
			}
			columnTypes.set( column, type );
			paste.setEnabled( false );
			switch ( type ) {
				case TEXT:
					table.getColumnModel().getColumn( column ).setCellEditor( new DefaultCellEditor( new JTextField() ) );
					table.getColumnModel().getColumn( column ).setCellRenderer( new DefaultTableCellRenderer() );
					break;
				case CHECK:
					table.getColumnModel().getColumn( column ).setCellEditor( table.getDefaultEditor( Boolean.class ) );
					table.getColumnModel().getColumn( column ).setCellRenderer( table.getDefaultRenderer( Boolean.class ) );
					break;
				case COMBO:
					table.getColumnModel().getColumn( column ).setCellEditor( new DefaultCellEditor( new JComboBox<String>( comboEntries ) ) );
					JComboBoxCellRenderer renderer = new JComboBoxCellRenderer();
					renderer.setModel( new DefaultComboBoxModel<String>( comboEntries ) );
					table.getColumnModel().getColumn( column ).setCellRenderer( renderer );
					break;
				case NUMBER:
					JTextField numberField = new JTextField();
					numberField.getDocument().addDocumentListener( new DocumentListener() {
						@Override
						public void removeUpdate( DocumentEvent e ) {}
						
						@Override
						public void insertUpdate( final DocumentEvent e ) {
							SwingUtilities.invokeLater( new Runnable() {
								@Override
								public void run() {
									try {
										Document doc = e.getDocument();
										String text = doc.getText( 0, doc.getLength() );
										for ( int i = 0; i < text.length(); i++ ) {
											if ( !Character.isDigit( text.charAt( i ) ) ) {
												text = text.substring( 0, i ) + text.substring( i + 1, text.length() );
												doc.remove( i, 1 );
												i--;
											}
										}
									} catch ( BadLocationException e1 ) {
										e1.printStackTrace();
									}
								}
							} );
							
						}
						
						@Override
						public void changedUpdate( DocumentEvent e ) {
							insertUpdate( e );
						}
					} );
					table.getColumnModel().getColumn( column ).setCellEditor( new DefaultCellEditor( numberField ) );
					table.getColumnModel().getColumn( column ).setCellRenderer( new DefaultTableCellRenderer() );
			}
		}
	}
	
	public void insertRow() {
		( (DefaultTableModel) table.getModel() ).addRow( new Vector<Object>() );
	}
	
	public void deleteRow( int row ) {
		if ( row >= 0 && row < table.getRowCount() ) {
			row = table.convertRowIndexToModel( row );
			( (DefaultTableModel) table.getModel() ).removeRow( row );
		}
	}
	
	public void copyRow( int row ) {
		if ( row >= 0 && row < table.getRowCount() ) {
			rowCopy = new Vector<Object>( table.getColumnCount() );
			for ( int col = 0; col < table.getColumnCount(); col++ ) {
				rowCopy.add( table.getValueAt( row, col ) );
			}
		}
	}
	
	public void pasteRow( int row ) {
		if ( row >= 0 && row < table.getRowCount() && rowCopy != null ) {
			table.editCellAt( -1, -1 );
			for ( int col = 0; col < table.getColumnCount(); col++ ) {
				table.setValueAt( rowCopy.get( col ), row, col );
			}
		}
	}
	
	public Vector<Vector<Object>> exportTableData() {
		Vector<Vector<Object>> target = new Vector<Vector<Object>>( table.getRowCount() );
		for ( int row = 0; row < table.getRowCount(); row++ ) {
			target.add( new Vector<Object>( table.getColumnCount() ) );
			for ( int col = 0; col < table.getColumnCount(); col++ ) {
				target.get( row ).add( table.getValueAt( row, col ) );
			}
		}
		return target;
	}
	
	public void saveTableToPath( String path ) {
		if ( !path.endsWith( EXTENSION ) ) {
			path += EXTENSION;
		}
		try {
			// Save table settings
			PrintWriter pw = new PrintWriter( new FileOutputStream( new File( path ) ) );
			pw.println( COMMENT + "Interactive JTable Save Data" );
			pw.println( "\n" + COMMENT + "Column Headers and Tooltips, the number of headers sets the number of columns:" );
			for ( int i = 0; i < table.getColumnCount(); i++ ) {
				pw.print( table.getColumnModel().getColumn( i ).getHeaderValue() + ( i == table.getColumnCount() - 1 ? "\n" : COMMA ) );
			}
			for ( int i = 0; i < table.getColumnCount(); i++ ) {
				pw.print( tooltips.get( i ) + ( i == table.getColumnCount() - 1 ? "\n" : COMMA ) );
			}
			pw.println( "\n" + COMMENT + "The following lines are all the data types of the columns" );
			pw.println( COMMENT + "There are 4 types: Text, Checkbox, Combo Box, and Number. Their syntax is as follows:" );
			pw.printf( "%s\"%s\"\n", COMMENT, CellType.TEXT.getTypeName() );
			pw.printf( "%s\"%s\"\n", COMMENT, CellType.CHECK.getTypeName() );
			pw.printf( "%s\"%s,choice,choice,choice,...\"\n", COMMENT, CellType.COMBO.getTypeName() );
			pw.printf( "%s\"%s\"\n", COMMENT, CellType.NUMBER.getTypeName() );
			pw.println( COMMENT + "The number of lines MUST equal the number of columns" );
			for ( int i = 0; i < table.getColumnCount(); i++ ) {
				switch ( columnTypes.get( i ) ) {
					case TEXT:
						pw.println( "text" );
						break;
					case CHECK:
						pw.println( "check" );
						break;
					case COMBO:
						pw.print( "combo," );
						JComboBox<String> combo = (JComboBox<String>) table.getColumnModel().getColumn( i ).getCellEditor().getTableCellEditorComponent( null, null, false, -1, i );
						for ( int j = 0; j < combo.getItemCount(); j++ ) {
							pw.print( combo.getItemAt( j ) + ( j == combo.getItemCount() - 1 ? "\n" : COMMA ) );
						}
						break;
					case NUMBER:
						pw.println( CellType.NUMBER.getTypeName() );
						break;
				}
			}
			pw.flush();
			pw.close();
			
			// Save current program
			int index = (int) indexSelector.getValue();
			if ( index > 0 ) {
				path = path.substring( 0, path.lastIndexOf( EXTENSION ) ) + "-" + index + PROGRAM_EXTENSION;
				pw = new PrintWriter( new FileOutputStream( new File( path ) ) );
				for ( int row = 0; row < table.getRowCount(); row++ ) {
					for ( int col = 0; col < table.getColumnCount(); col++ ) {
						pw.print( table.getValueAt( row, col ) + ( col == table.getColumnCount() - 1 ? "\n" : COMMA ) );
					}
				}
				pw.flush();
				pw.close();
			}
			uneditedData = exportTableData();
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
	}
	
	public void saveTableAs() {
		if ( browser.showSaveDialog( this ) == JFileChooser.APPROVE_OPTION ) {
			saveTableToPath( browser.getSelectedFile().getPath() );
			params.setEnabled( true );
			send.setEnabled( true );
			download.setEnabled( true );
			openFilePath = browser.getSelectedFile().getPath();
		}
	}
	
	public void loadTable() {
		if ( browser.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION ) {
			// Some initial setup
			params.setEnabled( true );
			send.setEnabled( true );
			download.setEnabled( true );
			openFilePath = browser.getSelectedFile().getPath();
			indexSelector.setValue( ( (SpinnerNumberModel) indexSelector.getModel() ).getMinimum() );
			try {
				uneditedData = null;
				Scanner scan = new Scanner( new File( File.separator + browser.getSelectedFile().getPath() ) );
				String[] data;
				// Get Column Headers
				data = nextAvailableLine( scan ).split( COMMA );
				final int cols = data.length;
				( (DefaultTableModel) table.getModel() ).setColumnCount( cols );
				( (DefaultTableModel) table.getModel() ).setColumnIdentifiers( data );
				data = nextAvailableLine( scan ).split( COMMA );
				tooltips = new Vector<String>( Arrays.asList( data ) );
				// Get Column Editor Types
				columnTypes = new Vector<CellType>( cols );
				for ( int i = 0; i < table.getColumnCount(); i++ ) {
					columnTypes.add( null );
					data = nextAvailableLine( scan ).split( COMMA );
					String type = data[0];
					if ( type.equals( CellType.TEXT.getTypeName() ) ) {
						setColumnType( i, CellType.TEXT );
					} else if ( type.equals( CellType.CHECK.getTypeName() ) ) {
						setColumnType( i, CellType.CHECK );
					} else if ( type.equals( CellType.COMBO.getTypeName() ) ) {
						setColumnType( i, CellType.COMBO, Arrays.copyOfRange( data, 1, data.length ) );
					} else if ( type.equals( CellType.NUMBER.getTypeName() ) ) {
						setColumnType( i, CellType.NUMBER );
					}
				}
				indexSelector.setValue( 1 );
				loadProgram( 1 );
				// resetSort();
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			}
		}
	}
	
	public void loadTableData( String path ) {
		try {
			Scanner scan = new Scanner( new File( path ) );
			String[] data;
			String line;
			( (DefaultTableModel) table.getModel() ).setRowCount( 0 );
			for ( int row = 0; scan.hasNext(); row++ ) {
				line = scan.nextLine();
				insertRow();
				data = line.split( COMMA );
				for ( int col = 0; col < table.getColumnCount(); col++ ) {
					switch ( columnTypes.get( col ) ) {
						case TEXT:
						case COMBO:
							table.getModel().setValueAt( data[col].equals( "null" ) ? null : data[col], row, col );
							break;
						case CHECK:
							String d = data[col].toLowerCase();
							table.getModel().setValueAt( !( d.equals( "null" ) || d.equals( "false" ) ), row, col );
							break;
						case NUMBER:
							table.setValueAt( data[col].equals( "null" ) ? null : Integer.parseInt( data[col] ), row, col );
							break;
					}
				}
			}
			scan.close();
			// uneditedData = exportTableData();
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
	}
	
	private String nextAvailableLine( Scanner scan ) {
		String line;
		while ( ( line = scan.nextLine() ).startsWith( COMMENT ) || line.length() == 0 ) {}
		return line;
	}
	
	private String getProgramBaseFileName() {
		return openFilePath.substring( 0, openFilePath.lastIndexOf( EXTENSION ) );
	}
	
	private String getTablePath() {
		return String.format( "%s%s", getProgramBaseFileName(), EXTENSION );
	}
	
	private String getProgramPath( int program ) {
		return String.format( "%s-%d%s", getProgramBaseFileName(), program, PROGRAM_EXTENSION );
	}
	
	private String getParamPath() {
		return String.format( "%s%s", getProgramBaseFileName(), ParameterEditor.PARAM_EXTENSION );
	}
}
