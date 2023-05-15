package se.iths.imageservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import se.iths.imageservice.entities.ImageEntity;
import se.iths.imageservice.mapper.FileWrapper;
import se.iths.imageservice.repository.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = ImageService.class)
@ContextConfiguration(classes = ImageService.class)
class ImageServiceTest {


    private static final String HOME_FOLDER = System.getProperty("user.home");
    private static final String IMAGE_FOLDER = "/var/lib/mysql/";
    private static final String FOLDER_PATH = HOME_FOLDER + IMAGE_FOLDER;
    public static final String CONTENT_TYPE = "image/jpeg";
    @Autowired
    ImageService service;
    @MockBean
    ImageRepository repo;
    @MockBean
    FileWrapper fileWrapper;

    MultipartFile file = mock(MultipartFile.class);

    @Test
    void uploadingANewImageShouldReturnCorrectAGeneratedID() throws IOException {
        var imageName = "mockImage.jpeg";

        var pathToImage = new File(FOLDER_PATH + imageName);
        var entity = ImageEntity.builder()
                .id(1L)
                .name(imageName)
                .type(CONTENT_TYPE)
                .build();


        when(repo.save(any(ImageEntity.class))).thenReturn(entity);
        when(file.getOriginalFilename()).thenReturn(imageName);
        when(file.getContentType()).thenReturn(CONTENT_TYPE);
        doNothing().when(file).transferTo(pathToImage);

        var result = service.uploadImage(file);


        verify(file, times(1)).transferTo(pathToImage);
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void returnsCorrectImageByteCodeWhenCalled() {
        var filePath = "/path/to/mockImage.jpeg";
        var entity = new ImageEntity(1L, "mockImage.jpeg", filePath, CONTENT_TYPE);
        var expectedResponse = ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .contentType(MediaType.valueOf(CONTENT_TYPE))
                .body(filePath.getBytes());

        when(repo.findById(anyLong())).thenReturn(Optional.of(entity));
        when(fileWrapper.readBytes(Path.of(entity.getFilePath()))).thenReturn(filePath.getBytes());

        var result = service.getImg(1L);

        verify(repo).findById(1L);
        verify(fileWrapper).readBytes(Path.of(entity.getFilePath()));
        assertThat(result).isEqualTo(expectedResponse);

    }

}
