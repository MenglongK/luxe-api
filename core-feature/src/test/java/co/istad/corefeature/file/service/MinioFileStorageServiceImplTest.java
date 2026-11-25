package co.istad.corefeature.file.service;

import co.istad.corefeature.file.config.MinioConfiguration;
import co.istad.corefeature.file.dto.FileResponse;
import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioFileStorageServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfiguration minioConfig;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new MinioFileStorageServiceImpl(minioClient, minioConfig);
    }

    @Test
    @DisplayName("Should upload image successfully to MinIO")
    void uploadFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vicuna-blazer.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(minioConfig.getBucketName()).thenReturn("luxe-bucket");
        when(minioConfig.getUrl()).thenReturn("http://localhost:9000");

        FileResponse response = fileStorageService.uploadFile(file);

        assertNotNull(response);
        assertEquals("vicuna-blazer.jpg", response.originalFileName());
        assertEquals("image/jpeg", response.contentType());
        assertTrue(response.fileName().endsWith(".jpg"));
        assertTrue(response.fileUrl().startsWith("http://localhost:9000/luxe-bucket/"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when uploading empty file")
    void uploadFile_emptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> fileStorageService.uploadFile(file));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("empty"));
    }

    @Test
    @DisplayName("Should throw 415 UNSUPPORTED_MEDIA_TYPE for disallowed MIME type")
    void uploadFile_unsupportedMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/x-msdownload",
                "binary code".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> fileStorageService.uploadFile(file));

        assertEquals(415, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Unsupported file format"));
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when file exceeds maximum allowed size")
    void uploadFile_fileTooLarge() {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-image.png",
                "image/png",
                largeBytes
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> fileStorageService.uploadFile(file));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("exceeds maximum allowed size"));
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when filename has directory traversal sequence")
    void uploadFile_pathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../etc/passwd.jpg",
                "image/jpeg",
                "image bytes".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> fileStorageService.uploadFile(file));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("invalid path sequence"));
    }

    @Test
    @DisplayName("Should upload multiple files successfully")
    void uploadMultipleFiles_success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "img1.png", "image/png", "img1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "img2.webp", "image/webp", "img2".getBytes());

        when(minioConfig.getBucketName()).thenReturn("luxe-bucket");
        when(minioConfig.getUrl()).thenReturn("http://localhost:9000");

        List<FileResponse> responses = fileStorageService.uploadMultipleFiles(List.of(file1, file2));

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(minioClient, times(2)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Should load file as Spring Resource from MinIO")
    void loadFileAsResource_success() throws Exception {
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);
        when(minioConfig.getBucketName()).thenReturn("luxe-bucket");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        Resource resource = fileStorageService.loadFileAsResource("test-file.jpg");

        assertNotNull(resource);
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("Should delete file successfully from MinIO")
    void deleteFile_success() throws Exception {
        when(minioConfig.getBucketName()).thenReturn("luxe-bucket");

        fileStorageService.deleteFile("test-file.jpg");

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }
}
