package co.istad.media_service.service;

import io.minio.errors.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface FileService {

    String uploadFile(MultipartFile file) throws Exception;

    InputStreamResource getFile(String fileName) throws Exception;

    String deleteFile(String fileName) throws Exception;
}
