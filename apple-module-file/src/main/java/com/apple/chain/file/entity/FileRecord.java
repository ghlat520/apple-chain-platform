package com.apple.chain.file.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * File record entity.
 * Table: file_record
 */
@Getter
@Setter
@TableName("file_record")
public class FileRecord extends BaseEntity {

    /** Public opaque identifier (UUID, no extension). Exposed in URLs. */
    private String fileId;

    /** Physical filename on storage: {fileId}.{ext} */
    private String filename;

    /** Original name as supplied by the uploader (sanitised, no path). */
    private String originalName;

    /** Storage path (relative root for local, object key for OSS). */
    private String storagePath;

    /** Public URL path served back to the client. */
    private String url;

    /** Size in bytes. */
    private Long size;

    /** MIME type. */
    private String contentType;

    /**
     * Business tag, free-form, e.g. "growth-record" / "orchard-photo".
     * Used purely for bookkeeping; no business logic depends on it.
     */
    private String bizType;

    /** Optional business entity id this file is attached to. */
    private Long bizId;

    /** Uploader user id (from JWT, nullable for seed data). */
    private Long uploaderId;

    /** Upload timestamp (distinct from BaseEntity.createTime for audit clarity). */
    private LocalDateTime uploadTime;
}
