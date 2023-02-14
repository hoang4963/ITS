package com.its.econtract.services;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface ECFtpStorageService {
    boolean uploadFtp(InputStream inputStream, String pathFtp);

    CompletableFuture<String> uploadAsyncFtp(InputStream inputStream, String pathFtp);

    InputStream downloadFile(String file);

    boolean deleteFile(String file);
}
