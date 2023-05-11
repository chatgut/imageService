package se.iths.imageservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import se.iths.imageservice.entities.ImageEntity;
import se.iths.imageservice.mapper.FileWrapper;
import se.iths.imageservice.repository.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.net.URI;

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

    public byte[] getImage(String url, UriComponentsBuilder urlComp) {
        var uri = urlComp.path("/").build().toUri().toString();
        var id = Long.parseLong(url.substring(uri.length()));

        return repo.findById(id).map(this::getImageAsBytes).orElse(null);
    }

    private byte[] getImageAsBytes(ImageEntity file) {
        return this.fileWrapper.readBytes(Path.of(file.getFilePath()));
    }

}
