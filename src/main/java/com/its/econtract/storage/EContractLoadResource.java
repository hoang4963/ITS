package com.its.econtract.storage;

import java.io.InputStream;

public interface EContractLoadResource {
    InputStream readResource(String path);

    String writeResource(String path);
}
