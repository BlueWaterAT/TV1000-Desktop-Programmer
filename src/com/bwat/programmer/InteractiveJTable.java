//Man am I happy to get rid of this ugly class
//package com.bwat.programmer;
//
//import com.bwat.util.NetUtils;
//import com.bwat.util.SwingUtils;
//import net.schmizz.sshj.SSHClient;
//import net.schmizz.sshj.sftp.SFTPClient;
//import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.JDialog;
//import javax.swing.JFileChooser;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JProgressBar;
//import javax.swing.JScrollPane;
//import javax.swing.JSpinner;
//import javax.swing.JTable;
//import javax.swing.ListSelectionModel;
//import javax.swing.SpinnerNumberModel;
//import javax.swing.SwingUtilities;
//import javax.swing.event.TableModelEvent;
//import javax.swing.event.TableModelListener;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.table.DefaultTableModel;
//import java.awt.BorderLayout;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Scanner;
//import java.util.Vector;
//
//public class InteractiveJTable extends JPanel {
//    private final JTable table = new JTable(2, 14);
//    private JSpinner indexSelector = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
//
//
//
//
//    public InteractiveJTable() {
//        table.setModel(new DefaultTableModel(2, 14));
//
//        // Intial setup
////        setPreferredSize(new Dimension(950, 800));
////        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
////        table.setRowHeight(25);
//
//
//
//        getModel().addTableModelListener(new TableModelListener() {
//            @Override
//            public void tableChanged(TableModelEvent e) {
//                SwingUtilities.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (uneditedData != null && !uneditedData.equals(exportTableData())) {
//                            if (openFilePath != null) {
//                                saveTableToPath(openFilePath);
//                            }
//                        }
//                    }
//                });
//            }
//        });
//
//        setLayout(new BorderLayout());
//        JScrollPane scroll = new JScrollPane(table);
//        scroll.setRowHeaderView(new RowNumberHeader(table));
//        add(scroll);
//        add(controls, BorderLayout.SOUTH);
//    }
//
//
//
//    public void loadProgram(int prog) {
//        if (prog > 0) {
//            uneditedData = null;
//            ((DefaultTableModel) table.getModel()).setRowCount(2);
//            clearTable();
//            if (openFilePath != null) {
//                String progPath = openFilePath.substring(0, openFilePath.endsWith(EXTENSION) ? openFilePath.lastIndexOf(EXTENSION) : openFilePath.length()) + "-" + prog + PROGRAM_EXTENSION;
//                if (new File(progPath).exists()) {
//                    loadTableData(progPath);
//                    log.info("Program {} Successfully loaded", prog);
//                }
//            }
//            uneditedData = exportTableData();
//        }
//    }
//
//    public void clearTable() {
//        for (int row = 0; row < table.getRowCount(); row++) {
//            for (int col = 0; col < table.getColumnCount(); col++) {
//                table.setValueAt(null, row, col);
//            }
//        }
//    }
//
//    public void saveTableToPath(String path) {
//        if (!path.endsWith(EXTENSION)) {
//            path += EXTENSION;
//        }
//        try {
//            // Save table settings
//            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(path)));
//            pw.println(COMMENT + "Interactive JTable Save Data");
//            pw.println("\n" + COMMENT + "Column Headers and Tooltips, the number of headers sets the number of columns:");
//            for (int i = 0; i < table.getColumnCount(); i++) {
//                pw.print(table.getColumnModel().getColumn(i).getHeaderValue() + (i == table.getColumnCount() - 1 ? "\n" : COMMA));
//            }
//            for (int i = 0; i < table.getColumnCount(); i++) {
//                pw.print(tooltips.get(i) + (i == table.getColumnCount() - 1 ? "\n" : COMMA));
//            }
//            pw.println("\n" + COMMENT + "The following lines are all the data types of the columns");
//            pw.println(COMMENT + "There are 4 types: Text, Checkbox, Combo Box, and Number. Their syntax is as follows:");
//            pw.printf("%s\"%s\"\n", COMMENT, CellType.TEXT.getTypeName());
//            pw.printf("%s\"%s\"\n", COMMENT, CellType.CHECK.getTypeName());
//            pw.printf("%s\"%s,choice,choice,choice,...\"\n", COMMENT, CellType.COMBO.getTypeName());
//            pw.printf("%s\"%s\"\n", COMMENT, CellType.NUMBER.getTypeName());
//            pw.println(COMMENT + "The number of lines MUST equal the number of columns");
//            for (int i = 0; i < table.getColumnCount(); i++) {
//                switch (columnTypes.get(i)) {
//                    case TEXT:
//                        pw.println("text");
//                        break;
//                    case CHECK:
//                        pw.println("check");
//                        break;
//                    case COMBO:
//                        pw.print("combo,");
//                        JComboBox<String> combo = (JComboBox<String>) table.getColumnModel().getColumn(i).getCellEditor().getTableCellEditorComponent(null, null, false, -1, i);
//                        for (int j = 0; j < combo.getItemCount(); j++) {
//                            pw.print(combo.getItemAt(j) + (j == combo.getItemCount() - 1 ? "\n" : COMMA));
//                        }
//                        break;
//                    case NUMBER:
//                        pw.println(CellType.NUMBER.getTypeName());
//                        break;
//                }
//            }
//            pw.flush();
//            pw.close();
//
//            // Save current program
//            int index = (int) indexSelector.getValue();
//            if (index > 0) {
//                path = path.substring(0, path.lastIndexOf(EXTENSION)) + "-" + index + PROGRAM_EXTENSION;
//                pw = new PrintWriter(new FileOutputStream(new File(path)));
//                for (int row = 0; row < table.getRowCount(); row++) {
//                    for (int col = 0; col < table.getColumnCount(); col++) {
//                        pw.print(table.getValueAt(row, col) + (col == table.getColumnCount() - 1 ? "\n" : COMMA));
//                    }
//                }
//                pw.flush();
//                pw.close();
//            }
//            uneditedData = exportTableData();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void saveTableAs() {
//        if (browser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//            saveTableToPath(browser.getSelectedFile().getPath());
//            send.setEnabled(true);
//            download.setEnabled(true);
//            openFilePath = browser.getSelectedFile().getPath();
//        }
//    }
//

//
//    public void loadTableData(String path) {
//        try {
//            Scanner scan = new Scanner(new File(path));
//            String[] data;
//            String line;
//            ((DefaultTableModel) table.getModel()).setRowCount(0);
//            for (int row = 0; scan.hasNext(); row++) {
//                line = scan.nextLine();
//                insertRow();
//                data = line.split(COMMA);
//                for (int col = 0; col < table.getColumnCount(); col++) {
//                    switch (columnTypes.get(col)) {
//                        case TEXT:
//                        case COMBO:
//                            table.getModel().setValueAt(data[col].equals("null") ? null : data[col], row, col);
//                            break;
//                        case CHECK:
//                            String d = data[col].toLowerCase();
//                            table.getModel().setValueAt(!(d.equals("null") || d.equals("false")), row, col);
//                            break;
//                        case NUMBER:
//                            table.setValueAt(data[col].equals("null") ? null : Integer.parseInt(data[col]), row, col);
//                            break;
//                    }
//                }
//            }
//            scan.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//

//
//
//}