package com.bwat.programmer;

import com.bwat.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import static com.bwat.programmer.Constants.*;

public class ProgramTable extends JTable {
    Logger log = LoggerFactory.getLogger(getClass());

    // List of all column types
    private ArrayList<CellType> columnTypes = new ArrayList<CellType>();

    // List of all mouseover tooltip messages
    private ArrayList<String> tooltips = new ArrayList<String>();

    // Used for keeping track of which column header was clicked
    private int popupCol = 0;

    public ProgramTable(int numRows, int numColumns) {
        super(numRows, numColumns);
        initGUI();
    }

    /**
     * Initializes all GUI components contained in the table
     */
    void initGUI() {
        // Set all columns to TEXT and set a default tooltip
        for (int i = 0; i < getColumnCount(); i++) {
            columnTypes.add(null);
            setColumnType(i, CellType.TEXT);
            tooltips.add("Col " + i);
        }

        // Initialize all settings for the right click popup menu
        final JPopupMenu headerMenu = new JPopupMenu(); // Actual popup menu

        // Add a listener to show the menu on right click
        getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupCol = getTableHeader().columnAtPoint(e.getPoint());
                    headerMenu.show(getTableHeader(), e.getX(), e.getY());
                }
            }
        });

        // Add a listener to display tooltips on mouse movement over the column header
        getTableHeader().addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int col = getTableHeader().columnAtPoint(e.getPoint());
                getTableHeader().setToolTipText(tooltips.get(col));
            }
        });

        // Initialize everything inside the popup menu
        JMenu jmi_type = new JMenu("Column Type"); // Column type submenu

        // Column data type choices
        JMenuItem jmi_text = new JMenuItem("Text");
        JMenuItem jmi_check = new JMenuItem("Checkbox");
        JMenuItem jmi_combo = new JMenuItem("Combo Box");
        JMenuItem jmi_num = new JMenuItem("Number");

        // Text type
        jmi_text.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColumnType(popupCol, CellType.TEXT);
                repaint();
            }
        });

        // Checkbox type
        jmi_check.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColumnType(popupCol, CellType.CHECK);
                repaint();
            }
        });

        // Combo box type
        jmi_combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Ask how many choices should be in the combobox
                JSpinner numEntries = new JSpinner();
                if (JOptionPane.showConfirmDialog(null, new Object[]{"How many entries?", numEntries}, "Combo Box Options", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    // Generate that many JTextFields and ask for all of the choices
                    int ents = (int) numEntries.getValue();
                    if (ents > 0) {
                        // Array of JTextFields
                        JTextField[] inputs = new JTextField[ents];
                        for (int i = 0; i < ents; i++) {
                            inputs[i] = new JTextField();
                        }
                        // Show prompt
                        if (JOptionPane.showConfirmDialog(null, inputs, "Enter Combo Box Choices", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                            // Collect all the combo box choices
                            String[] entries = new String[ents];
                            for (int i = 0; i < ents; i++) {
                                entries[i] = inputs[i].getText();
                            }
                            // Set the column type
                            setColumnType(popupCol, CellType.COMBO, entries);
                        }
                    }
                }
                repaint();
            }
        });

        // Number type
        jmi_num.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColumnType(popupCol, CellType.NUMBER);
                repaint();
            }
        });
        jmi_type.add(jmi_text);
        jmi_type.add(jmi_check);
        jmi_type.add(jmi_combo);
        jmi_type.add(jmi_num);

        // Option to rename header
        JMenuItem jmi_rename = new JMenuItem("Rename");
        jmi_rename.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel dialog = new JPanel(new GridLayout(2, 2));
                JTextField name = new JTextField(), tooltip = new JTextField();
                dialog.add(new JLabel("Name:"));
                dialog.add(name);
                dialog.add(new JLabel("Tooltip:"));
                dialog.add(tooltip);
                if (JOptionPane.showConfirmDialog(null, dialog, "Column Settings", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    setColumnHeader(popupCol, name.getText());
                    tooltips.set(popupCol, tooltip.getText());
                }
                repaint();
            }
        });

        // Option to add column
        JMenuItem jmi_add = new JMenuItem("Add Column");
        final JMenuItem jmi_delete = new JMenuItem("Delete Column");
        jmi_add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel dialog = new JPanel(new GridLayout(2, 2));
                JTextField name = new JTextField(), tooltip = new JTextField();
                dialog.add(new JLabel("Name:"));
                dialog.add(name);
                dialog.add(new JLabel("Tooltip:"));
                dialog.add(tooltip);
                if (JOptionPane.showConfirmDialog(null, dialog, "Column Settings", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    getColumnModel().addColumn(new TableColumn());
                    columnTypes.add(null);
                    setColumnType(getColumnCount() - 1, CellType.TEXT);
                    setColumnHeader(getColumnCount() - 1, name.getText());
                    tooltips.add(tooltip.getText());
                    if (!jmi_delete.isEnabled()) {
                        jmi_delete.setEnabled(true);
                    }
                }
                repaint();
            }
        });

        // Option to delete column
        jmi_delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getColumnModel().removeColumn(getColumnModel().getColumn(popupCol));
                columnTypes.remove(popupCol);
                tooltips.remove(popupCol);
                if (getColumnCount() == 1) {
                    jmi_delete.setEnabled(false);
                }
                repaint();
            }
        });
        headerMenu.add(jmi_type);
        headerMenu.add(jmi_rename);
        headerMenu.add(jmi_add);
        headerMenu.add(jmi_delete);

        // Fix column types when reordering
        getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            public void columnMoved(TableColumnModelEvent e) {
                if (e.getFromIndex() != e.getToIndex()) {
                    columnTypes.add(e.getToIndex(), columnTypes.remove(e.getFromIndex()));
                    tooltips.add(e.getToIndex(), tooltips.remove(e.getFromIndex()));
// 					paste.setEnabled( false );
                }
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnMarginChanged(ChangeEvent e) {
            }

            public void columnAdded(TableColumnModelEvent e) {
            }
        });
    }

    /**
     * Copies all of the table data into a 2D list
     *
     * @return List of all table data
     */
    public ArrayList<ArrayList<Object>> exportTableData() {
        ArrayList<ArrayList<Object>> copy = new ArrayList<ArrayList<Object>>(getRowCount());
        for (int row = 0; row < getRowCount(); row++) {
            copy.add(new ArrayList<Object>(getColumnCount()));
            for (int col = 0; col < getColumnCount(); col++) {
                copy.get(row).add(getValueAt(row, col));
            }
        }
        return copy;
    }

    /**
     * Sets the name of a column
     *
     * @param column Column index
     * @param header Name of the column
     */
    public void setColumnHeader(int column, String header) {
        getColumnModel().getColumn(column).setHeaderValue(header);
    }

    /**
     * Sets a columns to a specific type
     *
     * @param column       Column index
     * @param type         Column type
     * @param comboEntries OPTIONAL, fill only if the type is COMBO, then this is the entries of the that
     *                     combo box
     */
    public void setColumnType(int column, CellType type, String... comboEntries) {
        if (MathUtils.inRange_in_ex(column, 0, getColumnCount())) {
            // Column type is changing, so clear all the rows
            for (int i = 0; i < getRowCount(); i++) {
                setValueAt(null, i, column);
            }
            columnTypes.set(column, type);

            // Set the model used for each column type
            switch (type) {
                // TEXT uses a JTextField
                case TEXT:
                    getColumnModel().getColumn(column).setCellEditor(new DefaultCellEditor(new JTextField()));
                    getColumnModel().getColumn(column).setCellRenderer(new DefaultTableCellRenderer());
                    break;
                // CHECK uses a Boolean, which get represented as a checkbox
                case CHECK:
                    getColumnModel().getColumn(column).setCellEditor(getDefaultEditor(Boolean.class));
                    getColumnModel().getColumn(column).setCellRenderer(getDefaultRenderer(Boolean.class));
                    break;
                // COMBO uses a JComboBox<String>
                case COMBO:
                    getColumnModel().getColumn(column).setCellEditor(new DefaultCellEditor(new JComboBox<String>(comboEntries)));
                    JComboBoxCellRenderer renderer = new JComboBoxCellRenderer();
                    renderer.setModel(new DefaultComboBoxModel<String>(comboEntries));
                    getColumnModel().getColumn(column).setCellRenderer(renderer);
                    break;
                // NUMBER is a JTextField with a dynamic check to make sure only numbers are entered
                case NUMBER:
                    JTextField numberField = new JTextField();

                    // Add a change listener to the textfield
                    numberField.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void removeUpdate(DocumentEvent e) {
                        }

                        @Override
                        public void insertUpdate(final DocumentEvent e) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // Make sure anything that isn't a number gets deleted
                                        Document doc = e.getDocument();
                                        String text = doc.getText(0, doc.getLength());
                                        for (int i = 0; i < text.length(); i++) {
                                            if (!Character.isDigit(text.charAt(i))) {
                                                text = text.substring(0, i) + text.substring(i + 1, text.length());
                                                doc.remove(i, 1);
                                                i--;
                                            }
                                        }
                                    } catch (BadLocationException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });

                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            insertUpdate(e);
                        }
                    });
                    getColumnModel().getColumn(column).setCellEditor(new DefaultCellEditor(numberField));
                    getColumnModel().getColumn(column).setCellRenderer(new DefaultTableCellRenderer());
            }
        }
    }

    /**
     * @param col Column index
     * @return The CellType of a column
     */
    public CellType getColumnType(int col) {
        // Range validation
        if (!MathUtils.inRange_in_ex(col, 0, columnTypes.size())) {
            log.error("Invalid column index: ", col);
            return null;
        }

        return columnTypes.get(col);
    }

    /**
     * Inserts a blank row at the end of the table
     */
    public void insertRow() {
        ((DefaultTableModel) getModel()).addRow(new Vector<Object>());
    }

    /**
     * Deletes a row from the table
     *
     * @param row Row index
     */
    public void deleteRow(int row) {
        if (row >= 0 && row < getRowCount()) {
            row = convertRowIndexToModel(row);
            ((DefaultTableModel) getModel()).removeRow(row);
        }
    }

    /**
     * Deletes all rows from the table
     */
    public void deleteAllRows() {
        ((DefaultTableModel) getModel()).setRowCount(0);
    }

    /**
     * Sets the value of a cell
     *
     * @param val Cell value
     * @param row Row index
     * @param col Column index
     */
    public void setValueAt(Object val, int row, int col) {
        getModel().setValueAt(val, row, col);
    }

    /**
     * @param row Row index
     * @param col Column index
     * @return Cell value at the given location
     */
    public Object getValueAt(int row, int col) {
        return getModel().getValueAt(row, col);
    }

    /**
     * Loads the JTB data and formats the table
     *
     * @param path Path to the JTB file
     */
    public void loadTableFromFile(String path) {
        try {
            Scanner scan = new Scanner(new File(File.separator + path)); // Open file stream
            String[] data; // Holds temp data
            // Read and set the column headers
            data = nextAvailableLine(scan).split(COMMA);
            ((DefaultTableModel) getModel()).setColumnCount(data.length); // Set the # of columns
            ((DefaultTableModel) getModel()).setColumnIdentifiers(data);

            // Read and set the column tooltips
            data = nextAvailableLine(scan).split(COMMA);
            tooltips = new ArrayList<String>(Arrays.asList(data));

            // Read and set all the column types
            columnTypes.clear();
            for (int i = 0; i < getColumnCount(); i++) {
                columnTypes.add(null); // Add type placeholder
                // Read the next line
                data = nextAvailableLine(scan).split(COMMA);

                // Set column type based on what was read
                String type = data[0];
                if (type.equals(CellType.TEXT.getTypeName())) {
                    setColumnType(i, CellType.TEXT);
                } else if (type.equals(CellType.CHECK.getTypeName())) {
                    setColumnType(i, CellType.CHECK);
                } else if (type.equals(CellType.COMBO.getTypeName())) {
                    // The line for COMBO contains all the entries, so these are set as well
                    setColumnType(i, CellType.COMBO, Arrays.copyOfRange(data, 1, data.length));
                } else if (type.equals(CellType.NUMBER.getTypeName())) {
                    setColumnType(i, CellType.NUMBER);
                }
            }
            log.info("JTB file \"{}\" successfully loaded", path);
        } catch (FileNotFoundException e) {
            log.info("JTB file \"{}\" not found", path);
            e.printStackTrace();
        }
    }

    /**
     * Reads the file until a line that is not a comment and not blank is found
     *
     * @param scan File Scanner
     * @return The next line that has any content
     */
    private String nextAvailableLine(Scanner scan) {
        String line;
        // Keep reading until a line is found
        while ((line = scan.nextLine()).startsWith(COMMENT) || line.length() == 0) ;
        return line;
    }

    public void saveTableToPath(String path) {
        if (!path.endsWith(EXTENSION)) {
            path += EXTENSION;
        }
        try {
            // Save table settings
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(path)));
            pw.println(COMMENT + "Interactive JTable Save Data");
            pw.println("\n" + COMMENT + "Column Headers and Tooltips, the number of headers sets the number of columns:");
            for (int i = 0; i < getColumnCount(); i++) {
                pw.print(getColumnModel().getColumn(i).getHeaderValue() + (i == getColumnCount() - 1 ? "\n" : COMMA));
            }
            for (int i = 0; i < getColumnCount(); i++) {
                pw.print(tooltips.get(i) + (i == getColumnCount() - 1 ? "\n" : COMMA));
            }
            pw.println("\n" + COMMENT + "The following lines are all the data types of the columns");
            pw.println(COMMENT + "There are 4 types: Text, Checkbox, Combo Box, and Number. Their syntax is as follows:");
            pw.printf("%s\"%s\"\n", COMMENT, CellType.TEXT.getTypeName());
            pw.printf("%s\"%s\"\n", COMMENT, CellType.CHECK.getTypeName());
            pw.printf("%s\"%s,choice,choice,choice,...\"\n", COMMENT, CellType.COMBO.getTypeName());
            pw.printf("%s\"%s\"\n", COMMENT, CellType.NUMBER.getTypeName());
            pw.println(COMMENT + "The number of lines MUST equal the number of columns");
            for (int i = 0; i < getColumnCount(); i++) {
                switch (columnTypes.get(i)) {
                    case TEXT:
                        pw.println("text");
                        break;
                    case CHECK:
                        pw.println("check");
                        break;
                    case COMBO:
                        pw.print("combo,");
                        JComboBox<String> combo = (JComboBox<String>) getColumnModel().getColumn(i).getCellEditor().getTableCellEditorComponent(null, null, false, -1, i);
                        for (int j = 0; j < combo.getItemCount(); j++) {
                            pw.print(combo.getItemAt(j) + (j == combo.getItemCount() - 1 ? "\n" : COMMA));
                        }
                        break;
                    case NUMBER:
                        pw.println(CellType.NUMBER.getTypeName());
                        break;
                }
            }
            pw.flush();
            pw.close();
            log.info("JTB file \"{}\" successfully saved", path);

            // Create a blank PRG file
            int index = PROGRAM_DEFAULT;
            if (index > 0) {
                path = path.substring(0, path.lastIndexOf(EXTENSION)) + "-" + index + PROGRAM_EXTENSION;
                pw = new PrintWriter(new FileOutputStream(new File(path)));
                pw.close();
                log.info("Blank PRG file successfully saved to \"{}\"", path);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
