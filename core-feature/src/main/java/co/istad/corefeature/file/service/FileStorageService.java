package co.istad.corefeature.file.service;

import co.istad.corefeature.file.dto.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    FileResponse uploadFile(MultipartFile file);

    List<FileResponse> uploadMultipleFiles(List<MultipartFile> files);

    Resource loadFileAsResource(String fileName);

    String getContentType(String fileName);

    void deleteFile(String fileName);
}
