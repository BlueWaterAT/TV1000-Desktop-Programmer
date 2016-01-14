package com.bwat.programmer;

import java.io.IOException;

import net.schmizz.sshj.sftp.SFTPClient;

public interface SFTPAction {
	void run(SFTPClient sftp) throws IOException;
}
