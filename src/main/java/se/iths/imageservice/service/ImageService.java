package se.iths.imageservice.service;

import net.coobird.thumbnailator.Thumbnails;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.iths.imageservice.config.ApplicationDto;
import se.iths.imageservice.entities.ImageEntity;
import se.iths.imageservice.mapper.FileWrapper;
import se.iths.imageservice.repository.ImageRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class ImageService {
    ImageRepository repo;
    FileWrapper fileWrapper;
    ApplicationDto dto;

    public ImageService(ImageRepository repo, FileWrapper fileWrapper, ApplicationDto dto) {
        this.repo = repo;
        this.fileWrapper = fileWrapper;
        this.dto = dto;
        checkIfPathExists();
    }

    private void checkIfPathExists() {
        if (!Files.exists(getPath()))
            createDirectory();
    }

    public Long uploadImage(MultipartFile file) {
        var entity = saveEntity(file);
        saveFileToSystem(file);

        return entity.getId();
    }

    private void createDirectory() {
        try {
            Files.createDirectories(getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Path getPath() {
        return Path.of(dto.getMode() + "/var/lib/images");
    }

    private ImageEntity saveEntity(MultipartFile file) {
        return repo.save(ImageEntity.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .filePath(getFilePath(file))
                .build());
    }

    private String getFilePath(MultipartFile file) {
        return getPath() + "/" + file.getOriginalFilename();
    }

    private void saveFileToSystem(MultipartFile file) {
        try {
            file.transferTo(new File(getFilePath(file)));
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

    private byte[] getThumbnailAsBytes(ImageEntity file) {
        return this.fileWrapper.readBytes(Path.of(file.getThumbnail()));
    }

    public ResponseEntity getThumbnail(Long id, int height, int width) {
        var optionalImage = repo.findById(id);

        if (optionalImage.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        var image = optionalImage.get();

        if (image.getThumbnail() == null)
            return getThumbnailResponse(image, height, width);
        else
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf(image.getType()))
                    .body(getThumbnailAsBytes(image));

    }

    private ResponseEntity<byte[]> getThumbnailResponse(ImageEntity image, int height, int width) {
        try {
            var thumbnail = createAndReturnThumbnail(image, height, width);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf(image.getType()))
                    .body(thumbnail);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] createAndReturnThumbnail(ImageEntity image, int height, int width) throws Exception {
        createThumbnailDirectoryIfNotExists();

        var thumbnail = convertToThumbnail(image, height, width);
        var thumbnailPath = getPath() + "/thumbnails/" + image.getName();

        Files.write(Path.of(thumbnailPath), thumbnail);
        savePathToEntity(image, thumbnailPath);
        return thumbnail;
    }

    private void savePathToEntity(ImageEntity image, String thumbnailPath) {
        image.setThumbnail(thumbnailPath);
        repo.save(image);
    }

    private byte[] convertToThumbnail(ImageEntity image, int height, int width) throws Exception {
        var img = ImageIO.read(new File(image.getFilePath()));
        return resizeImage(img, width, height);
    }

    private void createThumbnailDirectoryIfNotExists() throws IOException {
        var pathToThumbnails = Path.of(getPath() + "/thumbnails");
        if (!Files.exists(pathToThumbnails))
            Files.createDirectory(pathToThumbnails);
    }

    byte[] resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        var outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(targetWidth, targetHeight)
                .outputFormat("JPEG")
                .outputQuality(1)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }
}
