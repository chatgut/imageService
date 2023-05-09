package se.iths.imageservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.iths.imageservice.service.ImageService;

@RestController("/images")
public class ImageController {

    ImageService imageService;

    //TODO handle exceptions
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @PostMapping()
    String uploadImage(@RequestParam("image") MultipartFile file) {
        return imageService.uploadImage(file);
    }

    @GetMapping("/{id}")
    String getPath(@PathVariable("id") String id) {
        return imageService.getPath(Long.parseLong(id));
    }

    @GetMapping()
    ResponseEntity getImage(@RequestParam("id") Long id) {
        var da = imageService.getImage(id);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(da);


    }
}

