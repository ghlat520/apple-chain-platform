package com.apple.chain.file.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Storage backend abstraction.
 * Implementations today: {@link LocalFileStorageService}.
 * Planned: {@code OssFileStorageService} (Aliyun OSS).
 *
 * <p>Contract:
 * <ul>
 *   <li>{@link #store} persists the bytes and returns the physical path / object key.</li>
 *   <li>{@link #load} returns a fresh {@link InputStream}; caller must close.</li>
 *   <li>{@code storagePath} is opaque to callers — only the service understands it.</li>
 * </ul>
 */
public interface FileStorageService {

    StoredObject store(InputStream source, String filename, long size, String contentType) throws IOException;

    InputStream load(String storagePath) throws IOException;

    boolean exists(String storagePath);
}
