package co.istad.media_service.repository;

import co.istad.media_service.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByFileName(String fileName);

    Boolean existsByFileName(String fileName);

    void deleteByFileName(String fileName);

}
