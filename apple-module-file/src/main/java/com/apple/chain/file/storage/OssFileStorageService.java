package com.apple.chain.file.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Placeholder for Aliyun OSS / S3 backend.
 * Enabled when {@code apple.file.storage.type=oss}.
 *
 * <p><b>NOT wired as a Spring bean yet.</b> To activate, add
 * {@code @Service} + {@code @ConditionalOnProperty(prefix="apple.file.storage", name="type", havingValue="oss")}
 * and inject an OSS SDK client. Keeping this file unannotated avoids pulling the OSS SDK
 * into the dev/test classpath prematurely.
 */
public class OssFileStorageService implements FileStorageService {

    @Override
    public StoredObject store(InputStream source, String filename, long size, String contentType) throws IOException {
        throw new UnsupportedOperationException("OSS storage not yet implemented. Configure apple.file.storage.type=local.");
    }

    @Override
    public InputStream load(String storagePath) throws IOException {
        throw new UnsupportedOperationException("OSS storage not yet implemented. Configure apple.file.storage.type=local.");
    }

    @Override
    public boolean exists(String storagePath) {
        return false;
    }
}
