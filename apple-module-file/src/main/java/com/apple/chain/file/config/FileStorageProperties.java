package com.apple.chain.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Binds {@code apple.file.*} configuration to typed properties.
 *
 * <pre>
 * apple:
 *   file:
 *     storage:
 *       type: local          # local | oss
 *       local:
 *         root: /tmp/apple-chain-files
 *     max-size: 10485760     # 10 MB
 *     allowed-content-types:
 *       - image/jpeg
 *       - image/png
 *       - image/webp
 *       - image/heic
 *     url-prefix: /api/files
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "apple.file")
@Getter
@Setter
public class FileStorageProperties {

    private Storage storage = new Storage();
    private long maxSize = 10L * 1024 * 1024;
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/heic",
            "image/heif"
    );
    private String urlPrefix = "/api/files";

    @Getter
    @Setter
    public static class Storage {
        private String type = "local";
        private Local local = new Local();

        @Getter
        @Setter
        public static class Local {
            private String root = "/tmp/apple-chain-files";
        }
    }
}
