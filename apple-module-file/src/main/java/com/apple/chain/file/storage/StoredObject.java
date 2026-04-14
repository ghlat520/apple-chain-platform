package com.apple.chain.file.storage;

import lombok.Builder;
import lombok.Getter;

/**
 * Pointer to a physical object produced by a {@link FileStorageService#store}.
 * {@code storagePath} is backend-specific (relative path for local, object key for OSS).
 */
@Getter
@Builder
public class StoredObject {
    private final String storagePath;
    private final String filename;
    private final long size;
}
