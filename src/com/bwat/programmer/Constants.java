package com.bwat.programmer;

/**
 * Constants used throughout the program
 *
 * @author Kareem ElFaramawi
 */
public class Constants {
    //File format variables
    public final static String EXTENSION = ".jtb";
    public final static String PROGRAM_EXTENSION = ".prg";
    public final static String COMMA = ",";
    public final static String COMMENT = ";";
    public final static int PROGRAM_DEFAULT = 1;

    // FTP related variables
    public final static int SFTP_PORT = 22;
    public final static String SFTP_USER = "root";
    public final static String SFTP_PASS = "bwat1234";
    public final static String SFTP_REMOTE_DIR = "/hmi/prg/";
    public final static String SFTP_ALERT_FILE = "SFTPUPDATE";
    public final static String SFTP_ALERT_MSG = "UPDATE\n";
    public final static String IP_LIST_FILE = "IpAddress.txt";

    //GUI
    public final static float FONT_SIZE = 28.0f;
}
