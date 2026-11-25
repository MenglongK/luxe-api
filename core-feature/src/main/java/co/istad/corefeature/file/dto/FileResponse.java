package co.istad.corefeature.file.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileResponse(
        String fileName,
        String originalFileName,
        String fileUrl,
        String contentType,
        Long size,
        LocalDateTime uploadedAt
) {
}
