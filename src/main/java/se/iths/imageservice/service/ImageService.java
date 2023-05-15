package se.iths.imageservice.service;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.iths.imageservice.entities.ImageEntity;
import se.iths.imageservice.mapper.FileWrapper;
import se.iths.imageservice.repository.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class ImageService {
    private static final String HOME_FOLDER = System.getProperty("user.home");
    private static final String IMAGE_FOLDER = "/var/lib/mysql/";
    private static final String FOLDER_PATH = HOME_FOLDER + IMAGE_FOLDER;

    ImageRepository repo;
    FileWrapper fileWrapper;

    public ImageService(ImageRepository repo, FileWrapper fileWrapper) {
        this.repo = repo;
        this.fileWrapper = fileWrapper;
    }

    public Long uploadImage(MultipartFile file) {
        var entity = saveEntity(file);
        saveFileToSystem(file);

        return entity.getId();
    }

    private ImageEntity saveEntity(MultipartFile file) {
        return repo.save(ImageEntity.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .filePath(FOLDER_PATH + file.getOriginalFilename())
                .build());
    }

    private static void saveFileToSystem(MultipartFile file) {
        try {
            file.transferTo(new File(FOLDER_PATH + file.getOriginalFilename()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public ResponseEntity getImg(Long id) {
        var image = repo.findById(id);

        if (image.isPresent())
            return ResponseEntity.status(HttpStatus.OK)
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                    .contentType(MediaType.valueOf(image.get().getType()))
                    .body(getImageAsBytes(image.get()));
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    }

    private byte[] getImageAsBytes(ImageEntity file) {
        return this.fileWrapper.readBytes(Path.of(file.getFilePath()));
    }
}
