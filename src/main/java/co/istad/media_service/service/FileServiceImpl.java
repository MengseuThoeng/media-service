package co.istad.media_service.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;

    @Value("${minio.server.bucket-name}")
    private String minioBucketName;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        InputStream fileStream = file.getInputStream();

        // Check if bucket exists; create if not
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucketName).build());
        }

        // Upload the file
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(fileName)
                        .stream(fileStream, file.getSize(), -1)
                        .build()
        );

        return "File uploaded successfully: " + fileName;
    }

    @Override
    public InputStreamResource getFile(String fileName) throws Exception {
        try {
            // Fetch the file as an InputStream
            InputStream fileStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(fileName)
                            .build()
            );

            return new InputStreamResource(fileStream);
        } catch (Exception e) {
            // Log the error to make it easier to debug
            e.printStackTrace();
            throw new Exception("Error fetching the file from MinIO", e);
        }
    }

    @Override
    public String deleteFile(String fileName) throws Exception {
        try{
            // Delete the file from the bucket
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(fileName)
                            .build()
            );

            return "File deleted successfully: " + fileName;
        } catch (Exception e) {
            // Log the error to make it easier to debug
            e.printStackTrace();
            throw new Exception("Error deleting the file from MinIO", e);
        }
    }
}
