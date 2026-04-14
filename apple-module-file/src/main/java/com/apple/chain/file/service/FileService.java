package com.apple.chain.file.service;

import com.apple.chain.file.dto.FileUploadResponse;
import com.apple.chain.file.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * File orchestration service — validates upload, delegates persistence to a
 * {@link com.apple.chain.file.storage.FileStorageService}, and records metadata
 * in {@code file_record}.
 */
public interface FileService {

    /**
     * Persist the upload. Performs size / content-type validation and DB recording.
     *
     * @param file    multipart payload (non-null, non-empty)
     * @param bizType optional business tag (e.g. "growth-record")
     * @return response with {@code fileId}, public {@code url}, and metadata
     */
    FileUploadResponse upload(MultipartFile file, String bizType);

    /**
     * Look up record by public {@code fileId}.
     * @throws com.apple.chain.common.exception.BizException when not found
     */
    FileRecord findByFileId(String fileId);

    /**
     * Open an InputStream on the physical object backing {@code fileId}.
     * Caller is responsible for closing the stream.
     */
    InputStream openStream(FileRecord record);
}
