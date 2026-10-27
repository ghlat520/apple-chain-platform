package com.apple.chain.file.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response payload returned by {@code POST /api/files/upload}.
 */
@Getter
@Builder
public class FileUploadResponse {

    /** Opaque file identifier (UUID, no extension). */
    private final String fileId;

    /** Public URL clients should embed / download. */
    private final String url;

    /** Physical filename on storage ({fileId}.{ext}). */
    private final String filename;

    /** Size in bytes. */
    private final Long size;

    /** MIME type. */
    private final String contentType;

    /** Business tag the uploader supplied (nullable). */
    private final String bizType;

    /** Upload timestamp. */
    private final LocalDateTime uploadTime;
}
