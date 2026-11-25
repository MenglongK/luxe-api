package co.istad.corefeature.file.service;

import co.istad.corefeature.file.config.MinioConfiguration;
import co.istad.corefeature.file.dto.FileResponse;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfiguration minioConfig;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml",
            "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public FileResponse uploadFile(MultipartFile file) {
        validateFile(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed"
        );
        String extension = "";
        int dotIdx = originalName.lastIndexOf('.');
        if (dotIdx > 0) {
            extension = originalName.substring(dotIdx);
        }

        String uniqueFileName = UUID.randomUUID() + extension;
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(uniqueFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            String fileUrl = String.format("%s/%s/%s",
                    minioConfig.getUrl(),
                    minioConfig.getBucketName(),
                    uniqueFileName
            );

            log.info("File uploaded successfully to MinIO: {}", uniqueFileName);

            return FileResponse.builder()
                    .fileName(uniqueFileName)
                    .originalFileName(originalName)
                    .fileUrl(fileUrl)
                    .contentType(contentType)
                    .size(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file to storage: " + e.getMessage());
        }
    }

    @Override
    public List<FileResponse> uploadMultipleFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File list cannot be empty");
        }

        List<FileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadFile(file));
        }
        return responses;
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
            return new InputStreamResource(stream);
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + fileName);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving file from MinIO");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file stream: " + e.getMessage());
        }
    }

    @Override
    public String getContentType(String fileName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
            return stat.contentType();
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
            log.info("File deleted from MinIO: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file from storage: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot upload an empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported file format. Allowed formats: JPEG, PNG, WEBP, GIF, SVG, PDF");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename contains invalid path sequence");
        }
    }
}
