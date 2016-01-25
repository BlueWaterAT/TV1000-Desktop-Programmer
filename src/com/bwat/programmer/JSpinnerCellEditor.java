package com.bwat.programmer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.table.TableCellEditor;

class JSpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
	final JSpinner spinner = new JSpinner();
	
	public JSpinnerCellEditor( SpinnerModel model ) {
		spinner.setModel( model );
	}
	
	public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column ) {
		spinner.setValue( value );
		return spinner;
	}
	
	public boolean isCellEditable( EventObject evt ) {
		if ( evt instanceof MouseEvent ) {
			return ( (MouseEvent) evt ).getClickCount() >= 2;
		}
		return true;
	}
	
	public Object getCellEditorValue() {
		return spinner.getValue();
	}
}
