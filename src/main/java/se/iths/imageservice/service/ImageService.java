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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.FormatFlagsConversionMismatchException;
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

    public ResponseEntity getThumbnail(Long id) {
        var i = repo.findById(id);
        var image = i.get();
        if (image.getThumbnail() == null) {

            try {
               if(!Files.exists(Path.of(FOLDER_PATH + "thumbnails")))
                   Files.createDirectory(Path.of(FOLDER_PATH + "thumbnails"));

               BufferedImage img = ImageIO.read(new File(image.getFilePath()));
                var s = resizeImage(img, 50, 50);

                String thumbnailPath = FOLDER_PATH + "thumbnails/" + image.getName();
                Files.write(Path.of(thumbnailPath), s);
                image.setThumbnail(thumbnailPath);
                repo.save(image);

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.valueOf(image.getType()))
                        .body(s);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf(image.getType()))
                    .body(getThumbnailAsBytes(image));
        }
    }

    byte[] resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        var outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(targetWidth, targetHeight)
                .outputFormat("JPEG")
                .outputQuality(1)
                .toOutputStream(outputStream);
        byte[] data = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        return data;
    }
}
