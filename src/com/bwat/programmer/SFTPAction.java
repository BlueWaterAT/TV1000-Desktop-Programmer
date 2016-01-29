package com.bwat.programmer;

import net.schmizz.sshj.sftp.SFTPClient;

import java.io.IOException;

public interface SFTPAction {
    void run(SFTPClient sftp) throws IOException;
}
