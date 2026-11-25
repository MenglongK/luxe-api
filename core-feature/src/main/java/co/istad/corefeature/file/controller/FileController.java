package co.istad.corefeature.file.controller;

import co.istad.corefeature.common.response.ApiResponse;
import co.istad.corefeature.file.dto.FileResponse;
import co.istad.corefeature.file.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/files", "/api/v1/file"})
@RequiredArgsConstructor
@Tag(name = "File Controller", description = "Endpoints for uploading and serving images and media via MinIO Object Storage")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload single file", description = "Uploads a single image or document to MinIO bucket")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestPart("file") MultipartFile file
    ) {
        FileResponse response = fileStorageService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("File uploaded successfully", response));
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple files", description = "Uploads multiple images simultaneously for product galleries")
    public ResponseEntity<ApiResponse<List<FileResponse>>> uploadMultipleFiles(
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<FileResponse> responses = fileStorageService.uploadMultipleFiles(files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Files uploaded successfully", responses));
    }

    @GetMapping("/{fileName}")
    @Operation(summary = "Preview file", description = "Streams the image file directly for browser preview")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = fileStorageService.getContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Download file", description = "Downloads file with attachment disposition")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = fileStorageService.getContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileName}")
    @Operation(summary = "Delete file", description = "Permanently removes file from MinIO storage")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.ok(ApiResponse.ok("File deleted successfully"));
    }
}
