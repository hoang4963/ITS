package com.its.econtract.services.impl;

import com.its.econtract.services.ECFtpStorageService;
import com.its.econtract.utils.ECDateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class ECFtpStorageServiceImpl implements ECFtpStorageService {

    @Value("${ftp.server}")
    private String server;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;


    private FTPClient openConnection() {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(server, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            int reply = ftpClient.getReplyCode();
            log.info("replyCode: {}", reply);
            if (ftpClient.isConnected()) {
                log.info("FTP connected");
            }
            return ftpClient;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeFTP(FTPClient ftpClient) {
        if (ftpClient == null) return;
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ex) {
            log.warn("Cause:", ex);
        }
    }

    @Override
    public boolean uploadFtp(InputStream inputStream, String pathFTP) {
        FTPClient ftpClient = openConnection();
        try {
            log.info("begin connect to FTP server");
            long begin = ECDateUtils.currentTimeMillis();
            log.info("begin upload file to FTP {}", begin);
            boolean done = ftpClient.storeFile(pathFTP, inputStream);
            long end = ECDateUtils.currentTimeMillis();
            log.info("end upload file to FTP {} duration = {}", end, (end - begin));
            return done;
        } catch (IOException ex) {
            log.warn("Can not upload FTP {}: ", pathFTP,  ex);
            throw new RuntimeException("Can not upload FTP");
        } finally {
            closeFTP(ftpClient);
        }
    }

    @Async
    @Override
    public CompletableFuture<String> uploadAsyncFtp(InputStream inputStream, String ftpPath) {
        FTPClient ftpClient = openConnection();
        try {
            long begin = ECDateUtils.currentTimeMillis();
            log.info("begin upload file to FTP {}", begin);
            boolean done = ftpClient.storeFile(ftpPath, inputStream);
            long end = ECDateUtils.currentTimeMillis();
            log.info("end upload file to FTP {} duration = {}", end, (end - begin));
            return CompletableFuture.completedFuture(done ? ftpPath : "");
        } catch (IOException ex) {
            throw new RuntimeException("Can not upload FTP");
        } finally {
            closeFTP(ftpClient);
        }
    }

    @Override
    public InputStream downloadFile(String file) {
        FTPClient ftpClient = openConnection();
        try {
            InputStream inputStream = ftpClient.retrieveFileStream(file);
            return inputStream;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeFTP(ftpClient);
        }
    }

    @Override
    public boolean deleteFile(String file) {
        FTPClient ftpClient = openConnection();
        try {
            boolean deleteFile = ftpClient.deleteFile(file);
            return deleteFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeFTP(ftpClient);
        }
    }
}
