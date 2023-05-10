package se.iths.imageservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import se.iths.imageservice.entities.ImageEntity;
import se.iths.imageservice.repository.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.net.URI;

@Service
public class ImageService {
    private static final String HOME_FOLDER = System.getProperty("user.home");
    private static final String IMAGE_FOLDER = "/var/lib/mysql/";
    private static final String FOLDER_PATH = HOME_FOLDER + IMAGE_FOLDER;

    ImageRepository repo;

    //TODO handle exceptions
    public ImageService(ImageRepository repo) {
        this.repo = repo;
    }


    public Long uploadImage(MultipartFile file) {
        checkIfPathExist();
        var entity = repo.save(ImageEntity.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .filePath(FOLDER_PATH + file.getOriginalFilename())
                .build());

        try {
            file.transferTo(new File(FOLDER_PATH + file.getOriginalFilename()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        entity.getId();

        return entity.getId();
    }

    private void checkIfPathExist() {
        if (Files.exists(getPath())) {
            return;
        } else {
            try {
                Files.createDirectories(getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static Path getPath() {
        return Path.of(FOLDER_PATH);
    }


    public byte[] getImage(Long id) {

        return getImageAsBytes(repo.findById(id));


    }

    public byte[] getImages(String url, UriComponentsBuilder ur) {
        URI uris = ur.path("/").build().toUri();
        String uri = uris.toString();
        Long id = Long.parseLong(url.substring(uri.length(), url.length()));

        return getImageAsBytes(repo.findById(id));

    }

    private static byte[] getImageAsBytes(Optional<ImageEntity> file) {
        try {
            return Files.readAllBytes(new File(file.get().getFilePath()).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
