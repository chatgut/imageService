package se.iths.imageservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import se.iths.imageservice.service.ImageService;


@RestController()
@RequestMapping("/images")
@CrossOrigin(maxAge = 3600)
public class ImageController {

    ImageService imageService;

    //TODO handle exceptions
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @PostMapping()
    String uploadImage(@RequestParam("image") MultipartFile file, UriComponentsBuilder ur) {
        var id = imageService.uploadImage(file);

        return ur.path("/" + id).build().toUri().toString();
    }


    @GetMapping()
    ResponseEntity getImages(@RequestParam("url") String url, UriComponentsBuilder ur) {
        var image = imageService.getImage(url, ur);

        if (image == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/png"))
                    .body(image);

    }
}

