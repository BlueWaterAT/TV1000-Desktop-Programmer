package com.bwat.programmer;

import net.schmizz.sshj.sftp.SFTPClient;

import java.io.IOException;

/**
 * An action to be run within an SFTP thread
 *
 * @author Kareem ElFaramawi
 */
public interface SFTPAction {

    /**
     * The SFTP action to be run
     *
     * @param sftp SFTP client
     * @throws IOException
     */
    void run(SFTPClient sftp) throws IOException;
}
