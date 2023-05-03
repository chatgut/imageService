package se.iths.imageservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.iths.imageservice.entities.ImageEntity;
import se.iths.imageservice.repository.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ImageService {
    private static final String HOME_FOLDER = System.getProperty("user.home");
    private static final String IMAGE_FOLDER = "/images";
    private static final String FOLDER_PATH = HOME_FOLDER + IMAGE_FOLDER;
    ImageRepository repo;

    public ImageService(ImageRepository repo) {
        this.repo = repo;
    }


    public String uploadImage(MultipartFile file) {
        checkIfPathExist();
        ImageEntity entity = repo.save(ImageEntity.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .filePath(FOLDER_PATH + file.getOriginalFilename())
                .build());

        try {
            file.transferTo(new File(FOLDER_PATH + "/" + file.getOriginalFilename()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return entity.toString();
    }

    private void checkIfPathExist() {
        if (Files.exists(getPath())) {
            return;
        } else {
            try {
                Files.createDirectory(getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Path getPath() {
        return Path.of(FOLDER_PATH);
    }
}
