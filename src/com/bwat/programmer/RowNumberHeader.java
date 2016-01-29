package com.bwat.programmer;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class RowNumberHeader extends JTable {
    private JTable parent;
    private int offset = 0; // An offset for the row #

    public RowNumberHeader(JTable table) {
        super();
        parent = table;
        setAutoCreateColumnsFromModel(false);
        setModel(parent.getModel());
        setSelectionModel(parent.getSelectionModel());
        setAutoscrolls(false);

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

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
