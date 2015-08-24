package com.bwat.programmer;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


public class JComboBoxCellRenderer extends JComboBox implements TableCellRenderer {
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
		setSelectedItem( value );
		return this;
	}
}
