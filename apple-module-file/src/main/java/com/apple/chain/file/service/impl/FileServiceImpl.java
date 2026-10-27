package com.apple.chain.file.service.impl;

import com.apple.chain.common.context.UserContext;
import com.apple.chain.common.exception.BizException;
import com.apple.chain.file.config.FileStorageProperties;
import com.apple.chain.file.dto.FileUploadResponse;
import com.apple.chain.file.entity.FileRecord;
import com.apple.chain.file.enums.FileErrorCode;
import com.apple.chain.file.mapper.FileRecordMapper;
import com.apple.chain.file.service.FileService;
import com.apple.chain.file.storage.FileStorageService;
import com.apple.chain.file.storage.StoredObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Default {@link FileService} — delegates to the configured storage backend.
 *
 * <p>Security responsibilities centralised here:
 * <ul>
 *   <li>Reject empty uploads, oversize payloads, and content-types outside the whitelist.</li>
 *   <li>Generate a random {@code fileId} (UUID, no hyphens) and strip the original filename
 *       to prevent path traversal / collision.</li>
 *   <li>Record uploader userId from {@link UserContext} so every blob has an owner.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageService storage;
    private final FileStorageProperties properties;
    private final FileRecordMapper fileRecordMapper;

    @Override
    public FileUploadResponse upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BizException(FileErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > properties.getMaxSize()) {
            throw new BizException(FileErrorCode.FILE_TOO_LARGE);
        }
        String contentType = normaliseContentType(file.getContentType());
        if (!properties.getAllowedContentTypes().contains(contentType)) {
            throw new BizException(FileErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String originalName = sanitise(file.getOriginalFilename());
        String extension = extractExtension(originalName, contentType);
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String physicalFilename = extension.isEmpty() ? fileId : fileId + "." + extension;

        StoredObject stored;
        try (InputStream in = file.getInputStream()) {
            stored = storage.store(in, physicalFilename, file.getSize(), contentType);
        } catch (IOException e) {
            log.error("[file] store failed", e);
            throw new BizException(FileErrorCode.FILE_STORAGE_ERROR);
        }

        String url = properties.getUrlPrefix() + "/" + fileId;
        FileRecord record = new FileRecord();
        record.setFileId(fileId);
        record.setFilename(physicalFilename);
        record.setOriginalName(originalName);
        record.setStoragePath(stored.getStoragePath());
        record.setUrl(url);
        record.setSize(stored.getSize());
        record.setContentType(contentType);
        record.setBizType(StringUtils.hasText(bizType) ? bizType : null);
        record.setUploaderId(UserContext.getUserId());
        record.setUploadTime(LocalDateTime.now());
        fileRecordMapper.insert(record);

        return FileUploadResponse.builder()
                .fileId(fileId)
                .url(url)
                .filename(physicalFilename)
                .size(stored.getSize())
                .contentType(contentType)
                .bizType(record.getBizType())
                .uploadTime(record.getUploadTime())
                .build();
    }

    @Override
    public FileRecord findByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            throw new BizException(FileErrorCode.FILE_NOT_FOUND);
        }
        FileRecord record = fileRecordMapper.selectOne(
                new LambdaQueryWrapper<FileRecord>().eq(FileRecord::getFileId, fileId));
        if (record == null) {
            throw new BizException(FileErrorCode.FILE_NOT_FOUND);
        }
        return record;
    }

    @Override
    public InputStream openStream(FileRecord record) {
        try {
            return storage.load(record.getStoragePath());
        } catch (IOException e) {
            log.error("[file] load failed id={} path={}", record.getFileId(), record.getStoragePath(), e);
            throw new BizException(FileErrorCode.FILE_STORAGE_ERROR);
        }
    }

    private static String normaliseContentType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "application/octet-stream";
        }
        int semi = raw.indexOf(';');
        return (semi >= 0 ? raw.substring(0, semi) : raw).trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Keep only the base name (no path) and drop characters that can confuse the
     * filesystem. The stored physical filename is {@code {uuid}.{ext}}, so this
     * sanitised value is only used for audit/download-headers.
     */
    private static String sanitise(String original) {
        if (!StringUtils.hasText(original)) {
            return "unnamed";
        }
        String base = original;
        int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        base = base.replaceAll("[\\x00-\\x1f\"<>|?*]", "_");
        if (base.length() > 120) {
            base = base.substring(0, 120);
        }
        return base.isEmpty() ? "unnamed" : base;
    }

    private static String extractExtension(String originalName, String contentType) {
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            String ext = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
            if (ext.matches("[a-z0-9]{1,8}")) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/heic", "image/heif" -> "heic";
            default -> "";
        };
    }
}
