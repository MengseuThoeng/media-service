package co.istad.media_service.controller;


import co.istad.media_service.dto.FileResponse;
import co.istad.media_service.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<Map<String, String>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files) {
        try {
            // Call the service to upload multiple files
            List<Map<String, String>> response = fileService.uploadMultipleFiles(files);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response if something goes wrong
            return ResponseEntity.status(500).body(List.of(Map.of(
                    "error", "Failed to upload files",
                    "reason", e.getMessage()
            )));
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload")
    Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        return fileService.uploadFile(file);

    }

    @GetMapping("/all")
    ResponseEntity<?> getAllFiles() throws Exception {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    @GetMapping("/{fileName}")
    FileResponse getFileByName(@PathVariable String fileName) {
        try {
            return fileService.getFileByName(fileName);
        } catch (Exception e) {
            return null;
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
    Map<String, String> deleteFile(@RequestParam String fileName) throws Exception {
        return fileService.deleteFile(fileName);

    }
}
