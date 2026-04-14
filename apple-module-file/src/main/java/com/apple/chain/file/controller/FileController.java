package com.apple.chain.file.controller;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.R;
import com.apple.chain.file.dto.FileUploadResponse;
import com.apple.chain.file.entity.FileRecord;
import com.apple.chain.file.enums.FileErrorCode;
import com.apple.chain.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * File upload / download endpoints.
 *
 * <p>Upload is authenticated (covered by {@code AuthInterceptor} via the {@code /api/**} rule);
 * download by opaque {@code fileId} is exposed under {@code /api/files/**} so authenticated
 * clients can embed URLs in DTOs (e.g. {@code photoUrls}).
 */
@Slf4j
@Tag(name = "文件上传/下载")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件（图片）")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileUploadResponse> upload(
            @Parameter(description = "文件内容（必填）", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型，如 growth-record")
            @RequestParam(value = "bizType", required = false) String bizType) {
        return R.ok(fileService.upload(file, bizType));
    }

    @Operation(summary = "按 fileId 下载文件")
    @GetMapping("/{fileId}")
    public void download(@PathVariable String fileId, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(fileId) || !fileId.matches("[a-zA-Z0-9]{1,64}")) {
            throw new BizException(FileErrorCode.FILENAME_INVALID);
        }
        FileRecord record = fileService.findByFileId(fileId);
        response.setContentType(record.getContentType());
        if (record.getSize() != null) {
            response.setContentLengthLong(record.getSize());
        }
        String downloadName = record.getOriginalName() != null ? record.getOriginalName() : record.getFilename();
        String encoded = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=3600");

        try (InputStream in = fileService.openStream(record);
             OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }
}
