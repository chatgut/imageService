package se.iths.imageservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import se.iths.imageservice.service.ImageService;

@RestController("/imageService")
public class ImageController {

    ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @PostMapping("/upload")
    Long uploadImage(@RequestParam("image") MultipartFile file) {

        return imageService.uploadImage(file);
    }

    @GetMapping("/{id}")
    String getPath(@RequestParam("id") Long id){
        return imageService.getPath(id);
    }
}

