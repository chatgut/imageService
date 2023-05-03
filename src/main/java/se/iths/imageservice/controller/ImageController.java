package se.iths.imageservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import se.iths.imageservice.service.ImageService;

@RestController("/images")
public class ImageController {

    ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    String uploadImage() {
        imageService.uploadImage();
        return "";
    }
}

