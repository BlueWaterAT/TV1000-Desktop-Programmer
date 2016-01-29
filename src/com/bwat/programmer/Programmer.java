package com.bwat.programmer;

import com.bwat.util.MathUtils;
import com.bwat.util.NetUtils;
import com.bwat.util.SwingUtils;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import static com.bwat.programmer.Constants.*;

public class Programmer extends JPanel {
    Logger log = LoggerFactory.getLogger(getClass());

    // The paged table interface
    PagedProgramTable paged;

    // The table contained in PagedProgramTable
    ProgramTable table;

    // Holds a copy of a row's data
    private ArrayList<Object> rowCopy = new ArrayList<Object>();

    private ArrayList<ArrayList<Object>> uneditedData; // TODO: Is this needed?

    // File IO
    private JFileChooser browser = new JFileChooser();
    private String openFilePath = null;

    // FTP related variables
    private final static int SFTP_PORT = 22;
    private final static String SFTP_USER = "root";
    private final static String SFTP_PASS = "bwat1234";
    private final static String SFTP_REMOTE_DIR = "/hmi/prg/";
    private final static String IP_LIST_FILE = "IpAddress.txt";

    // GUI
    private JButton insert = new JButton("Insert Row");
    private JButton delete = new JButton("Delete Row");
    private JButton copy = new JButton("Copy Row");
    private JButton paste = new JButton("Paste Row");
    private JButton saveAs = new JButton("Save As...");
    private JButton load = new JButton("Load Table");
    private JButton send = new JButton("Send");
    private JButton download = new JButton("Download");

    public Programmer() {
        paged = new PagedProgramTable();
        table = paged.getTable();
        initGUI();
        uneditedData = table.exportTableData();
    }

    /**
     * Initializes all of the GUI components in the TV1000 Programmer
     */
    private void initGUI() {
        // File browser settings
        browser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        browser.setFileFilter(new FileFilter() {
            public String getDescription() {
                return "JTable Data";
            }

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(EXTENSION);
            }
        });

        // Programmer Controls

        // Initial button states
        paste.setEnabled(false);
        send.setEnabled(false);
        download.setEnabled(false);

        // Initialize the control panel
        JPanel controls = new JPanel(new GridLayout(1, 5));
        controls.setPreferredSize(new Dimension(getWidth(), 150));

        // Button action listeners

        // INSERT
        insert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table.insertRow();
            }
        });

        // DELETE
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: Row deletion
                log.error("NOT IMPLEMENTED YET: CAREFUL WHICH ROW IS DELETED");
                System.out.println(paged.getSelectedRow());
//                table.deleteRow(table.getSelectedRow());
            }
        });

        // COPY
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyRow(table.getSelectedRow());
            }
        });

        // PASTE
        paste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                pasteRow(table.getSelectedRow());
            }
        });

        // SAVE AS
        saveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                saveTableAs();
            }
        });

        // LOAD
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadTable();
            }
        });

        // SEND
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the initial IP
                String ip = promptIPAddress();
                if (ip != null) {
                    // Split the IP, separating the last number as an int
                    String base = ip.substring(0, ip.lastIndexOf('.'));
                    int vNum = Integer.parseInt(ip.substring(ip.lastIndexOf('.') + 1));

                    // Prompt for how many vehicles to send to
                    JSpinner vCount = new JSpinner(new SpinnerNumberModel(1, 1, 256 - vNum, 1));
                    if (JOptionPane.showConfirmDialog(null, new Object[]{"How many vehicles do you want to send the program to?", vCount}, "Send Program", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        // Send to all vehicles
                        for (int i = 0, c = (int) vCount.getValue(); i < c; i++) {
                            sendFile(base + "." + (vNum + i));
                        }
                    }

                }
            }
        });

        // DOWNLOAD
        download.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadFile(promptIPAddress());
            }
        });

        // Assemble the control panel
        controls.add(SwingUtils.createGridJPanel(2, 1, insert, saveAs));
        controls.add(SwingUtils.createGridJPanel(2, 1, delete, load));
        controls.add(SwingUtils.createGridJPanel(2, 1, copy, send));
        controls.add(SwingUtils.createGridJPanel(2, 1, paste, download));
        SwingUtils.setFont_r(controls, controls.getFont().deriveFont(28.0f).deriveFont(Font.BOLD));

        // Add everything
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(950, 800));
        add(paged, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

    }

    /**
     * Copies the data from a row into a list
     *
     * @param row Row index
     */
    public void copyRow(int row) {
        // Range validation
        if (MathUtils.inRange_in_ex(row, 0, table.getRowCount())) {
            paste.setEnabled(true);
            rowCopy.clear();
            for (int col = 0; col < table.getColumnCount(); col++) {
                rowCopy.add(table.getValueAt(row, col));
            }
        } else {
            log.error("Invalid row index to copy from: {}", row);
        }
    }

    // TODO: Fix saving here
    //????????
    //    public void pasteRow(int row) {
//        if (row >= 0 && row < table.getRowCount() && rowCopy.size() == table.getColumnCount()) {
//            table.editCellAt(-1, -1);
//            for (int col = 0; col < table.getColumnCount(); col++) {
//                table.setValueAt(rowCopy.get(col), row, col);
//            }
//        }
//    }

    /**
     * Prompts the user for the SFTP IP address.
     * Previously used addresses are loaded from a file
     * If a new, valid IP is given, it is saved as well
     *
     * @return The IP address
     */
    private String promptIPAddress() {
        // Prompt setup
        String host = null;
        // IP combo box
        JComboBox<String> ipSelect = new JComboBox<String>();
        ipSelect.setEditable(true); // Allows manual entry
        ipSelect.setMaximumRowCount(10);

        try {
            // Attempt to load the IP addresses from the file
            File ipList = new File(IP_LIST_FILE);

            // Save them in a set to keep uniqueness
            HashSet<String> ips = new HashSet<String>();
            if (ipList.exists()) {
                // Read every line and make sure it is a valid IP
                Scanner scan = new Scanner(ipList);
                String ip;
                while (scan.hasNext()) {
                    ip = scan.nextLine();
                    if (NetUtils.isValidIPAddress(ip)) {
                        ips.add(ip);
                        ipSelect.addItem(ip);
                    }
                }
                scan.close();
                log.info("IPs loaded from {}", IP_LIST_FILE);
            } else {
                log.info("{} not found, nothing loaded", IP_LIST_FILE);
            }

            // Prompt the user for the IP address
            if (JOptionPane.showConfirmDialog(null, new Object[]{"Enter vehicle IP address", ipSelect}, "Send Program", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                // Validate the IP address
                String ip = ipSelect.getSelectedItem().toString();
                if (NetUtils.isValidIPAddress(ip)) {
                    host = ip;
                    ips.add(host); // Add it to the list of all IPs
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid IP Address!");
                }
            }

            if (!ips.isEmpty()) {
                // Save all of the IP addresses back to the file
                PrintWriter pw = new PrintWriter(ipList);
                for (String ip : ips) {
                    pw.println(ip);
                }
                pw.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return host;
    }

    /**
     * Connects to the vehicle and runs a general SFTP command
     *
     * @param host        Vehicle IP address
     * @param action      The SFTP action to execute on the vehicle
     * @param progressMsg Message to show while connecting
     * @param success     Message to show on success
     */
    private void runSFTPCommand(final String host, final SFTPAction action, final String progressMsg, final String success) {
        // Validate the IP address
        if (host != null && NetUtils.isValidIPAddress(host)) {
            // Display a loading dialog
            final JDialog sending = new JDialog();
            sending.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            sending.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);

            // Infinite progress bar
            JProgressBar progress = new JProgressBar();
            progress.setIndeterminate(true);

            sending.add(SwingUtils.createGridJPanel(2, 1, new JLabel(progressMsg), progress));
            sending.pack();
            sending.setLocationRelativeTo(null);

            new Thread() {
                public void run() {
                    // TODO: Fix saving here
                    // Save the table
//                    saveTableToPath(openFilePath);

                    String msg = ""; // Holds the result message
                    try {
                        // Open connection
                        SSHClient ssh = new SSHClient();
                        ssh.addHostKeyVerifier(new PromiscuousVerifier());
                        ssh.connect(host, SFTP_PORT);
                        ssh.authPassword(SFTP_USER, SFTP_PASS);
                        SFTPClient sftp = ssh.newSFTPClient();

                        // Run the SFTP action
                        action.run(sftp);

                        // Close the connection
                        sftp.close();
                        ssh.close();

                        msg = success;
                        log.debug(msg);
                    } catch (final IOException e) {
                        msg = " SFTP Error: " + e.getMessage();
                        log.error(msg);
                    } finally {
                        // Hide loading dialog and show result message
                        sending.setVisible(false);
                        JOptionPane.showMessageDialog(Programmer.this, msg);
                    }
                }
            }.start();
            sending.setVisible(true); // Show the loading dialog
        } else {
            JOptionPane.showMessageDialog(null, "Invalid IP Address!");
            log.error("SFTP Error: {} is not a valid IP address", host);
        }
    }

    /**
     * Downloads the PRG file from a vehicle and replaces the one currently being used
     *
     * @param host Vehicle IP address
     */
    private void downloadFile(final String host) {
        runSFTPCommand(host, new SFTPAction() {
            @Override
            public void run(SFTPClient sftp) throws IOException {
                // Local PRG file handle
                File prg = new File(getProgramPath(PROGRAM_DEFAULT));

                // Download the PRG file
                String remote = SFTP_REMOTE_DIR + prg.getName();
                sftp.get(remote, prg.getPath());

                // Reload the program data
                paged.reloadProgram();
            }
        }, "Downloading from " + host + "...", "Program download successful!");
    }

    /**
     * Sends the local PRG file to a vehicle, overwriting the remote PRG file
     *
     * @param host Vehicle IP address
     */
    private void sendFile(final String host) {
        runSFTPCommand(host, new SFTPAction() {
            @Override
            public void run(SFTPClient sftp) throws IOException {
                File prg = new File(getProgramPath(PROGRAM_DEFAULT));

                // Send the PRG file
                String remote = SFTP_REMOTE_DIR + prg.getName();
                sftp.put(prg.getPath(), remote);
            }
        }, "Sending to " + host + "...", "Program upload successful!");
    }

    /**
     * @return Get the base name of the working file (no extension)
     */
    private String getProgramBaseFileName() {
        return openFilePath.substring(0, openFilePath.lastIndexOf(EXTENSION));
    }

    /**
     * @return Gets the path to the open JTB file
     */
    private String getTablePath() {
        return String.format("%s%s", getProgramBaseFileName(), EXTENSION);
    }

    /**
     * NOTE: The ID feature has been removed, but the file still uses this format
     *
     * @param program Program ID
     * @return Path to the PRG file with the given ID
     */
    private String getProgramPath(int program) {
        return String.format("%s-%d%s", getProgramBaseFileName(), program, PROGRAM_EXTENSION);
    }

    /**
     * Prompts the user to browse to a JTB file and loads it
     */
    public void loadTable() {
        // Show file browser
        if (browser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Some initial setup
            send.setEnabled(true);
            download.setEnabled(true);
            paste.setEnabled(false);
            rowCopy.clear();
            uneditedData = null;

            // Load the JTB and PRG
            openFilePath = browser.getSelectedFile().getPath();
            table.loadTableFromFile(openFilePath);
            paged.loadProgram(getProgramPath(PROGRAM_DEFAULT));
        }
    }
}
