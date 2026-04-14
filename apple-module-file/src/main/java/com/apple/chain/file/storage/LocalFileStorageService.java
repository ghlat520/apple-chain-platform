package com.apple.chain.file.storage;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.file.config.FileStorageProperties;
import com.apple.chain.file.enums.FileErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Local disk storage backend.
 * Enabled when {@code apple.file.storage.type=local} (default).
 *
 * <p>Layout: {root}/{yyyy}/{MM}/{dd}/{filename}
 * The dated sub-directories keep per-day file count bounded and make cleanup trivial.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "apple.file.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileStorageProperties properties;

    @Override
    public StoredObject store(InputStream source, String filename, long size, String contentType) throws IOException {
        String relativeDir = LocalDate.now().format(DATE_DIR);
        Path root = Paths.get(properties.getStorage().getLocal().getRoot()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(relativeDir).normalize();
        if (!targetDir.startsWith(root)) {
            throw new BizException(FileErrorCode.FILE_STORAGE_ERROR);
        }
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(filename);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        long actualSize = Files.size(target);
        String storagePath = relativeDir + "/" + filename;
        log.info("[file] stored local path={} size={} contentType={}", storagePath, actualSize, contentType);
        return StoredObject.builder()
                .storagePath(storagePath)
                .filename(filename)
                .size(actualSize)
                .build();
    }

    @Override
    public InputStream load(String storagePath) throws IOException {
        Path target = resolve(storagePath);
        if (!Files.exists(target)) {
            throw new BizException(FileErrorCode.FILE_NOT_FOUND);
        }
        return Files.newInputStream(target);
    }

    @Override
    public boolean exists(String storagePath) {
        try {
            return Files.exists(resolve(storagePath));
        } catch (BizException ex) {
            return false;
        }
    }

    private Path resolve(String storagePath) {
        Path root = Paths.get(properties.getStorage().getLocal().getRoot()).toAbsolutePath().normalize();
        Path target = root.resolve(storagePath).normalize();
        // Path-traversal guard: storagePath must stay inside the configured root.
        if (!target.startsWith(root)) {
            throw new BizException(FileErrorCode.FILENAME_INVALID);
        }
        return target;
    }
}
