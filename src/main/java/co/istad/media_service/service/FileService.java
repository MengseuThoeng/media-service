package co.istad.media_service.service;

import co.istad.media_service.dto.FileResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileService {

    Map<String,String> uploadFile(MultipartFile file) throws Exception;

    InputStreamResource getFile(String fileName) throws Exception;

    Map<String,String> deleteFile(String fileName) throws Exception;

    FileResponse getFileByName(String fileName);

    List<FileResponse> getAllFiles();

    List<Map<String,String>> uploadMultipleFiles(MultipartFile[] files) throws Exception;
}
