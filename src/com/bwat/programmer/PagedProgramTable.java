package com.bwat.programmer;

import com.bwat.util.MathUtils;
import com.bwat.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static com.bwat.programmer.Constants.*;

/**
 * A JTable interface that dynamically loads and saves data at certain sections of a PRG file at a time
 * NOTE: If the PRG file is ever changed the file MUST be reindexed
 *
 * @author Kareem El-Faramawi
 */
public class PagedProgramTable extends JPanel {
    Logger log = LoggerFactory.getLogger(getClass());

    // Path to the PRG file
    String programPath;

    // Number of elements to display on a page
    int pageSize = 25;

    // Currently displayed page number
    int currentPage;

    // Maps row numbers to byte indices in the PRG file
    ArrayList<Long> idxs = new ArrayList<Long>();

    // Cache of the data for the currently displayed rows
    ArrayList<ArrayList<Object>> displayedRows = new ArrayList<ArrayList<Object>>();

    // GUI
    final ProgramTable table = new ProgramTable(2, 14);
    // Page controls
    JPanel ctrlPanel = new JPanel(new BorderLayout());
    JButton jumpPrev = new JButton("\u2190");
    JButton jumpPage = new JButton("");
    JButton jumpNext = new JButton("\u2192");
    JButton jumpRow = new JButton("Jump to Row");
    RowNumberHeader rowHeader;

    public PagedProgramTable() {
        initGUI();
        jumpToPage(1);
    }

    /**
     * Initializes the GUI components for this PagedProgramTable
     * TODO: Add an option to set pageSize
     */
    private void initGUI() {
        // JTable settings
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);

        // Action listeners for all buttons

        // Previous page
        jumpPrev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prevPage();
            }
        });

        // Next page
        jumpNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextPage();
            }
        });

        // Jump to a page
        jumpPage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Prompt setup
                JSpinner pageSelect = new JSpinner(new SpinnerNumberModel(currentPage, 1, getNumPages(), 1));
                JPanel spinnerPanel = new JPanel(new BorderLayout());
                spinnerPanel.add(pageSelect, BorderLayout.CENTER);
                spinnerPanel.add(new JLabel(" / " + getNumPages()), BorderLayout.EAST);

                // Prompt the user for the page # they want to jump to
                if (JOptionPane.showConfirmDialog(null, new Object[]{"Enter page number", spinnerPanel}, "Jump to Page", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    jumpToPage((int) pageSelect.getValue()); // Jump to the selected page
                }
            }
        });

        // Jump to a page containing a certain row
        jumpRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getNumRows() > 0) { // Nothing to jump to
                    // Prompt setup
                    JSpinner rowSelect = new JSpinner(new SpinnerNumberModel(getFirstRowOnPage(currentPage) + 1, 1, getNumRows(), 1));
                    JPanel spinnerPanel = new JPanel(new BorderLayout());
                    spinnerPanel.add(rowSelect, BorderLayout.CENTER);
                    spinnerPanel.add(new JLabel(" / " + getNumRows()), BorderLayout.EAST);

                    // Prompt the user for the row # they want to jump to
                    if (JOptionPane.showConfirmDialog(null, new Object[]{"Enter row number", spinnerPanel}, "Jump to Row", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        jumpToRow((int) rowSelect.getValue());
                    }
                }
            }
        });

        // Assemble control panel
        ctrlPanel.add(SwingUtils.createGridJPanel(1, 3, jumpPrev, jumpPage, jumpNext), BorderLayout.WEST);
        ctrlPanel.add(jumpRow, BorderLayout.EAST);
        SwingUtils.setFont_r(ctrlPanel, ctrlPanel.getFont().deriveFont(28.0f).deriveFont(Font.BOLD));

        // Scroll panel for the table with headers
        JScrollPane scroll = new JScrollPane(table);
        rowHeader = new RowNumberHeader(table);
        scroll.setRowHeaderView(rowHeader);

        // Add everything
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(ctrlPanel, BorderLayout.SOUTH);
    }

    /**
     * @return The number of pages that the program has been divided into
     */
    public int getNumPages() {
        return (int) Math.max(1, Math.ceil((double) getNumRows() / pageSize)); // TODO: Make sure this works
    }

    /**
     * @return The total number of rows in the program
     */
    public int getNumRows() {
        return idxs.size() - 1; //-1 because the end index is included in idxs, which doesn't count as a row
    }

    public int getFirstRowOnPage(int page) {
        return pageSize * (page - 1);
    }

    /**
     * Repopulates the table with the data from the cache, and updates displayed information.
     * Assumes that currentPage and displayedRows have been updated
     */
    public void reloadDisplay() {
        // Update the JTable data
        table.deleteAllRows(); // Delete all displayed rows
        // Iterate through the row cache
        for (int row = 0; row < displayedRows.size(); row++) {
            table.insertRow(); // Add the new row
            // Fill the table data with the cached data
            ArrayList<Object> rowData = displayedRows.get(row);
            for (int col = 0; col < rowData.size(); col++) {
                table.setValueAt(rowData.get(col), row, col);
            }
        }

        // Update page size
        jumpPage.setText(String.format("Page %d / %d", currentPage, getNumPages()));

        // Update row header numbers
        rowHeader.setOffset(getFirstRowOnPage(currentPage));
    }

    /**
     * Reindexes the program file
     */
    public void reloadProgram() {
        // Clear old indices
        idxs.clear();
        try {
            // Load file
            RandomAccessFile f = getFile();
            if (f != null) {
                // Read line by line, saving every stream position
                long len = f.length();
                while (f.getFilePointer() != len) {
                    idxs.add(f.getFilePointer());
                    f.readLine();
                }
                // Save the index for the end of the file
                idxs.add(f.getFilePointer());
            }
        } catch (IOException e) {
            log.error("Error while reading PRG file \"{}\"", programPath);
            e.printStackTrace();
        }
        log.info("PRG file \"{}\" successfully indexed", programPath);
    }

    /**
     * Loads, indexes, and displays a PRG file
     *
     * @param path Path to the PRG file
     */
    public void loadProgram(String path) {
        if (new File(path).exists()) {
            programPath = path;
            reloadProgram();
            jumpToPage(1);
        } else {
            log.error("PRG file \"{}\" not found", path);
        }
    }

    public void savePage() {
        // TODO: savePage
    }

    public void deleteRow(int tableIndex) {
        // TODO: deleteRow
    }

    /**
     * Inserts a blank new row at the very end of the program and saves the PRG file
     */
    public void insertRow() {
        // Load the file
        RandomAccessFile f = getFile();
        if (f != null) {
            try {
                // Create a blank row string ("null,null,null,null,....")
                String newRow = new String(new char[table.getColumnCount() - 1]).replace("\0", "null,") + "null\n";

                // Check to make sure the file ending is correct
                if (f.length() > 0) {
                    // Seek to the last character in the file
                    f.seek(idxs.get(idxs.size() - 1) - 1);

                    // Make sure there's a newline before the new row
                    if (f.read() != (byte) '\n') {
                        newRow = "\n" + newRow;
                    }
                }

                // Write the new row to the end of the file
                f.setLength(f.length() + newRow.length());
                f.write(newRow.getBytes());
                f.close();

                log.info("New row inserted at the end of \"{}\"", programPath);
            } catch (IOException e) {
                log.info("Error inserting new row in \"{}\"", programPath);
                e.printStackTrace();
            }

            // Reload the program
            reloadProgram();
            jumpToPage(currentPage);
        }
    }

    /**
     * Sets the number of elements to display on every page and updates the display
     *
     * @param size Number of elements per page
     */
    public void setPageSize(int size) {
        // Size validation
        if (size < 1) {
            throw new IllegalArgumentException("Page size cannot be < 1");
        }

        // Reload display at the first page
        pageSize = size;
        jumpToPage(1);
    }

    /**
     * Loads the row information from the requested page and displays it
     * NOTE: This will force the requested page to be in the range [1, getNumPages()]
     *
     * @param p Page number to display
     */
    public void jumpToPage(int p) {
        // Auto clamp the given page number into an allowed range
        if (!MathUtils.inRange_in(p, 1, getNumPages())) {
            currentPage = MathUtils.clamp_i(p, 1, getNumPages());
            log.info("Page number was auto-clamped from {} to {}", p, currentPage);
        } else {
            currentPage = p;
        }

        // Load the page data
        loadPage(currentPage);

        // Show the page data and update display
        reloadDisplay();
    }

    /**
     * Jumps to the page containing a certain row number
     *
     * @param r Requested row number
     */
    public void jumpToRow(int r) {
        jumpToPage((int) Math.ceil((double) r / pageSize)); // TODO: Make sure this actually works. Make sure harder.
    }

    /**
     * Loads the next page
     */
    public void nextPage() {
        jumpToPage(currentPage + 1);
    }

    /**
     * Loads the previous page
     */
    public void prevPage() {
        jumpToPage(currentPage - 1);
    }

    /**
     * @return The internally used ProgramTable
     */
    public ProgramTable getTable() {
        return table;
    }

    /**
     * Gets the true row number for the visually selected row index
     *
     * @param displayed JTable index
     * @return Real row number in the whole program
     */
    public int getRowNumber(int displayed) {
        return displayed + pageSize * (currentPage - 1);
    }

    /**
     * @return The number of the currently selected row
     */
    public int getSelectedRow() {
        return getRowNumber(table.getSelectedRow());
    }

    /**
     * Gets the byte index in the program file that a row starts at
     *
     * @param r Requested row number
     * @return Starting index of the row data
     */
    private Long getRowByteIdx(int r) {
        // Row index validation
        if (!MathUtils.inRange_in_ex(r, 0, getNumRows())) {
            throw new IllegalArgumentException("The requested row (" + r + ") does not exist in the program");
        }

        // Get row's position index in the file
        return idxs.get(r);
    }

    /**
     * Loads the data of all the rows on a page into memory
     *
     * @param page
     */
    private void loadPage(int page) {
        // Clear the old cache
        displayedRows.clear();

        // Load the file
        RandomAccessFile f = getFile();
        if (f != null && getNumRows() > 0) { // Don't attempt to load if nothing is indexed
            try {
                long len = f.length(); // Cache the file length
                f.seek(getRowByteIdx(getFirstRowOnPage(page))); // Jump to the starting row

                // These hold data while parsing lines
                String line;
                String[] data;
                for (int i = 0; i < pageSize && f.getFilePointer() != len; i++) {
                    line = f.readLine(); // Read a row
                    data = line.split(COMMA); // Split it
                    ArrayList<Object> rowData = new ArrayList<Object>(); // Temp array
                    // Parse the data depending on the type of the column it belongs in
                    for (int col = 0; col < table.getColumnCount(); col++) {
                        switch (table.getColumnType(col)) {
                            // Simple text
                            case TEXT:
                            case COMBO:
                                rowData.add(data[col].equals("null") ? null : data[col]);
                                break;
                            // Boolean
                            case CHECK:
                                String d = data[col].toLowerCase();
                                rowData.add(!(d.equals("null") || d.equals("false")));
                                break;
                            // Integer
                            case NUMBER:
                                rowData.add(data[col].equals("null") ? null : Integer.parseInt(data[col]));
                                break;
                        }
                    }
                    displayedRows.add(rowData); // Add the row to the cache
                }
            } catch (IOException e) {
                log.error("Error while reading PRG file \"{}\"", programPath);
                e.printStackTrace();
            }
        }
    }

    /**
     * Attempts to load the file located at programPath
     *
     * @return The file handle if it was loaded, null otherwise
     */
    private RandomAccessFile getFile() {
        // Verify a file path has been set
        if (programPath == null || programPath.equals("")) {
            log.error("ERROR: No program loaded!");
            return null;
        }

        // Attempt to load the file
        try {
            return new RandomAccessFile(programPath, "rw");
        } catch (FileNotFoundException e) {
            log.error("PRG file \"{}\" not found", programPath);
            e.printStackTrace();
        }

        // File was not found
        return null;
    }

}
