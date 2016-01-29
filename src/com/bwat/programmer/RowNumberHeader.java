package com.bwat.programmer;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * A header to display row numbers on the side of a JTable
 *
 * @author Kareem ElFaramawi
 */
public class RowNumberHeader extends JTable {
    // Parent table this is displayed on
    private JTable parent;

    // An offset for the row #
    private int offset = 0;

    public RowNumberHeader(JTable table) {
        super();
        parent = table;

        // Some header settings
        setAutoCreateColumnsFromModel(false);
        setModel(parent.getModel());
        setSelectionModel(parent.getSelectionModel());
        setAutoscrolls(false);

        // Creating the header column
        TableColumn col = new TableColumn();
        addColumn(col);
        col.setCellRenderer(parent.getTableHeader().getDefaultRenderer());
        col.setPreferredWidth(50);
        setPreferredScrollableViewportSize(getPreferredSize());
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int column) {
        return offset + row + 1; // Add the offset so the row # is correct page to page
    }

    public int getRowHeight() {
        return parent.getRowHeight();
    }

    /**
     * Set the offset for the first row number
     *
     * @param offset Row offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
