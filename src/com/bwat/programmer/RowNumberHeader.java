package com.bwat.programmer;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class RowNumberHeader extends JTable {
	private JTable parent;
	
	public RowNumberHeader( JTable table ) {
		super();
		parent = table;
		setAutoCreateColumnsFromModel( false );
		setModel( parent.getModel() );
		setSelectionModel( parent.getSelectionModel() );
		setAutoscrolls( false );
		
		TableColumn col = new TableColumn();
		addColumn( col );
		col.setCellRenderer( parent.getTableHeader().getDefaultRenderer() );
		col.setPreferredWidth( 50 );
		setPreferredScrollableViewportSize( getPreferredSize() );
	}
	
	public boolean isCellEditable( int row, int column ) {
		return false;
	}
	
	public int getColumnCount() {
		return 1;
	}
	
	public Object getValueAt( int row, int column ) {
		return new Integer( row + 1 );
	}
	
	public int getRowHeight() {
		return parent.getRowHeight();
	}
}
