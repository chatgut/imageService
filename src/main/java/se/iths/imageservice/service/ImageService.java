package se.iths.imageservice.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
        var thumbnailPath = FOLDER_PATH + "thumbnails/" + image.getName();

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

    private static void createThumbnailDirectoryIfNotExists() throws IOException {
        var pathToThumbnails = Path.of(FOLDER_PATH + "thumbnails");
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
