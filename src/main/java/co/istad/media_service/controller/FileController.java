package co.istad.media_service.controller;


import co.istad.media_service.service.FileService;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String response = fileService.uploadFile(file);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/preview")
    public ResponseEntity<InputStreamResource> getImage(@RequestParam String fileName) {
        try {
            // Attempt to retrieve the file from the service
            InputStreamResource resource = fileService.getFile(fileName);

            // Return the file as a response with appropriate headers
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .contentType(MediaType.IMAGE_JPEG)// Change MIME type as per the file format
                    .body(resource);

        } catch (Exception e) {
            // Log the error and return a proper error response
            e.printStackTrace(); // Log the error to help diagnose
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/delete")
    ResponseEntity<?> deleteFile(@RequestParam String fileName) {
        try {
            String response = fileService.deleteFile(fileName);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
