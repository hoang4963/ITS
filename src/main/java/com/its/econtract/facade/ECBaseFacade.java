package com.its.econtract.facade;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
public class ECBaseFacade {

    protected void rmTempFiles(List<String> files) {
        try {
            Path path = null;
            for (String file : files) path = Paths.get(file);
            if (path != null) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            log.info("Can not remove temporary files");
        }
    }
}
