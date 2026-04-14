package com.apple.chain.file;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.file.config.FileStorageProperties;
import com.apple.chain.file.dto.FileUploadResponse;
import com.apple.chain.file.entity.FileRecord;
import com.apple.chain.file.enums.FileErrorCode;
import com.apple.chain.file.mapper.FileRecordMapper;
import com.apple.chain.file.service.impl.FileServiceImpl;
import com.apple.chain.file.storage.FileStorageService;
import com.apple.chain.file.storage.LocalFileStorageService;
import com.apple.chain.file.storage.StoredObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the upload/validation/download paths.
 * Storage layer is the real {@link LocalFileStorageService} against a @TempDir so
 * we exercise the filesystem guard-rails, but mapper/storage stay Mockito-isolated
 * where appropriate.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileServiceImpl 单元测试")
class FileServiceImplTest {

    @Mock
    private FileRecordMapper fileRecordMapper;

    private FileStorageProperties properties;
    private FileServiceImpl fileService;
    private LocalFileStorageService storage;

    @BeforeEach
    void setUp(@TempDir Path tempRoot) {
        properties = new FileStorageProperties();
        properties.getStorage().getLocal().setRoot(tempRoot.toString());
        properties.setMaxSize(1024L);
        // Re-use default allowedContentTypes (jpg/png/webp/heic)
        storage = new LocalFileStorageService(properties);
        fileService = new FileServiceImpl(storage, properties, fileRecordMapper);
    }

    @Nested
    @DisplayName("upload 正常路径")
    class UploadSuccess {

        @Test
        @DisplayName("上传 PNG 成功 — 生成 fileId/url，写入 file_record")
        void uploadPngSucceeds() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "photo.png", "image/png", new byte[]{1, 2, 3, 4});

            FileUploadResponse response = fileService.upload(file, "growth-record");

            assertThat(response.getFileId()).hasSize(32);
            assertThat(response.getUrl()).isEqualTo("/api/files/" + response.getFileId());
            assertThat(response.getFilename()).endsWith(".png");
            assertThat(response.getSize()).isEqualTo(4);
            assertThat(response.getContentType()).isEqualTo("image/png");
            assertThat(response.getBizType()).isEqualTo("growth-record");

            ArgumentCaptor<FileRecord> captor = ArgumentCaptor.forClass(FileRecord.class);
            verify(fileRecordMapper).insert(captor.capture());
            FileRecord saved = captor.getValue();
            assertThat(saved.getFileId()).isEqualTo(response.getFileId());
            assertThat(saved.getStoragePath()).endsWith(response.getFilename());
            assertThat(saved.getOriginalName()).isEqualTo("photo.png");
        }
    }

    @Nested
    @DisplayName("upload 校验失败")
    class UploadValidation {

        @Test
        @DisplayName("空文件 -> FILE_EMPTY")
        void emptyRejected() {
            MockMultipartFile empty = new MockMultipartFile(
                    "file", "x.png", "image/png", new byte[0]);
            assertThatThrownBy(() -> fileService.upload(empty, null))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(FileErrorCode.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("超出大小限制 -> FILE_TOO_LARGE")
        void oversizeRejected() {
            byte[] big = new byte[(int) properties.getMaxSize() + 1];
            MockMultipartFile file = new MockMultipartFile(
                    "file", "big.png", "image/png", big);
            assertThatThrownBy(() -> fileService.upload(file, null))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(FileErrorCode.FILE_TOO_LARGE.getCode());
        }

        @Test
        @DisplayName("禁止的 contentType -> FILE_TYPE_NOT_ALLOWED")
        void disallowedTypeRejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "bad.exe", "application/x-msdownload", new byte[]{1, 2, 3});
            assertThatThrownBy(() -> fileService.upload(file, null))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(FileErrorCode.FILE_TYPE_NOT_ALLOWED.getCode());
        }
    }

    @Nested
    @DisplayName("download 路径")
    class Download {

        @Test
        @DisplayName("按 fileId 打开 stream — 返回原字节")
        void openStreamReturnsBytes() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "x.jpg", "image/jpeg", new byte[]{9, 8, 7});
            FileUploadResponse response = fileService.upload(file, null);
            ArgumentCaptor<FileRecord> captor = ArgumentCaptor.forClass(FileRecord.class);
            verify(fileRecordMapper).insert(captor.capture());
            FileRecord saved = captor.getValue();

            when(fileRecordMapper.selectOne(any(Wrapper.class))).thenReturn(saved);

            FileRecord found = fileService.findByFileId(response.getFileId());
            try (InputStream in = fileService.openStream(found)) {
                assertThat(in.readAllBytes()).containsExactly(9, 8, 7);
            }
        }

        @Test
        @DisplayName("未知 fileId -> FILE_NOT_FOUND")
        void missingRejected() {
            when(fileRecordMapper.selectOne(any(Wrapper.class))).thenReturn(null);
            assertThatThrownBy(() -> fileService.findByFileId("nope"))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(FileErrorCode.FILE_NOT_FOUND.getCode());
        }
    }

    @Nested
    @DisplayName("LocalFileStorageService 直接")
    class StorageGuards {

        @Test
        @DisplayName("路径穿越 -> FILENAME_INVALID")
        void pathTraversalBlocked() {
            assertThatThrownBy(() -> storage.load("../etc/passwd"))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(FileErrorCode.FILENAME_INVALID.getCode());
        }

        @Test
        @DisplayName("store 产生带日期子目录的 path")
        void storeUsesDateDir() throws Exception {
            StoredObject stored = storage.store(
                    new java.io.ByteArrayInputStream(new byte[]{1}), "a.png", 1, "image/png");
            assertThat(stored.getStoragePath()).matches("\\d{4}/\\d{2}/\\d{2}/a\\.png");
            Path root = Path.of(properties.getStorage().getLocal().getRoot());
            assertThat(Files.exists(root.resolve(stored.getStoragePath()))).isTrue();
        }
    }

    // Accessor purely to keep the FileStorageService import live.
    @SuppressWarnings("unused")
    private FileStorageService typedStorage() {
        return storage;
    }
}
