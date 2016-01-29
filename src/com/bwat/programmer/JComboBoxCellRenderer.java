package com.bwat.programmer;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

/**
 * A TableCellRenderer to display a JComboBox in a cell
 *
 * @author Kareem ElFaramawi
 */
public class JComboBoxCellRenderer extends JComboBox implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setSelectedItem(value);
        return this;
    }
}
