package co.istad.media_service.service;

import co.istad.media_service.domain.File;
import co.istad.media_service.dto.FileResponse;
import co.istad.media_service.repository.FileRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;

    private final FileRepository fileRepository;

    @Value("${minio.server.bucket-name}")
    private String minioBucketName;

    @Value("${media.url}")
    private String mediaUrl;

    private boolean isValidFileExtension(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return fileExtension.equals("jpg") || fileExtension.equals("png") || fileExtension.equals("jpeg");
    }

    @Override
    public List<Map<String, String>> uploadMultipleFiles(MultipartFile[] files) throws Exception {
        if (files == null || files.length == 0) {
            throw new Exception("No files provided for upload.");
        }

        // Process each file and collect results
        return Stream.of(files)
                .map(file -> {
                    try {
                        // Call the single-file upload method for each file
                        return uploadFile(file);
                    } catch (Exception e) {
                        // Handle exceptions for each file and add an error message
                        return Map.of(
                                "error", "Failed to upload file",
                                "file_name", file.getOriginalFilename(),
                                "reason", e.getMessage()
                        );
                    }
                })
                .toList();
    }

    @Override
    public FileResponse getFileByName(String fileName) {
        File file = fileRepository.findByFileName(fileName).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")
        );

        return new FileResponse(
                file.getFileName(),
                file.getFileUrl(),
                file.getFileType(),
                file.getSize()
        );
    }

    @Override
    public List<FileResponse> getAllFiles() {
        return fileRepository.findAll().stream()
                .map(file -> new FileResponse(
                        file.getFileName(),
                        file.getFileUrl(),
                        file.getFileType(),
                        file.getSize()
                ))
                .toList();
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !isValidFileExtension(fileName)) {
            throw new Exception("Invalid file type. Only JPG, PNG, and JPEG are allowed.");
        }

        // Extract file name without extension and the file extension
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String fileExtension = fileName.substring(fileName.lastIndexOf('.'));

        // Generate a new file name with the current date and time
        String newFileName = baseName + "_" + LocalDateTime.now().toString().replace(":", "-") + fileExtension;

        // Ensure bucket exists
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucketName).build());
        }

        InputStream fileStream = file.getInputStream();

        // Upload file with the new name
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(newFileName) // Use the new file name
                        .stream(fileStream, file.getSize(), -1)
                        .build()
        );

        // Save file information to the database
        File newFile = new File();
        newFile.setFileName(newFileName);
        newFile.setFileType(file.getContentType());
        newFile.setSize((double) file.getSize());
        newFile.setFileUrl(mediaUrl + "?fileName=" + newFileName);
        fileRepository.save(newFile);


        return Map.of(
                "success", "File uploaded successfully",
                "file_name", newFileName
        );
    }


    @Override
    public InputStreamResource getFile(String fileName) throws Exception {
        try {
            InputStream fileStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(fileName)
                            .build()
            );

            return new InputStreamResource(fileStream);
        } catch (Exception e) {
            throw new Exception("Error fetching the file from MinIO", e);
        }
    }

    @Override
    public Map<String, String> deleteFile(String fileName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(fileName)
                            .build()
            );

            File file = fileRepository.findByFileName(fileName).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")
            );

            fileRepository.delete(file);


            return Map.of(
                    "success", "File deleted successfully",
                    "file_name", fileName
            );
        } catch (Exception e) {
            throw new Exception("Error deleting the file from MinIO", e);
        }
    }
}
