package co.istad.media_service.dto;

public record FileResponse(
//        Long id,
        String fileName,

        String fileUrl,

        String fileType,

        Double size
) {
}
