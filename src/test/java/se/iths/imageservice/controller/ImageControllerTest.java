package se.iths.imageservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import se.iths.imageservice.service.ImageService;

import java.io.FileInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
@ContextConfiguration(classes = ImageController.class)
class ImageControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ImageController imageController;
    @Autowired
    WebApplicationContext webApplicationContext;
    @MockBean
    ImageService imageService;

    @Test
    void upLoadingImageShouldreturn200Okandurl() throws Exception {
        var image = new MockMultipartFile
                ("image", "mockImage.jpg", "image/jpeg", "mockImage".getBytes());

        given(imageService.uploadImage(image)).willReturn(1L);


        mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
                        .file(image))
                .andExpect(status().isOk());

        verify(imageService, times(1)).uploadImage(image);
    }

    @Test
    void getImageShouldReturn200okIfImageExists() throws Exception {
        mockMvc.perform(get("/images/{id}", 1L))
                .andExpect(status().isOk());

        verify(imageService, times(1)).getImg(1L);
    }
}
