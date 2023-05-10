package se.iths.imageservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import se.iths.imageservice.service.ImageService;

import java.net.URI;

@RestController()
@RequestMapping("/images")
public class ImageController {

    ImageService imageService;

    //TODO handle exceptions
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @PostMapping()
    String uploadImage(@RequestParam("image") MultipartFile file, UriComponentsBuilder ur) {
        var id = imageService.uploadImage(file);
        URI uri = ur.path("/" + id).build().toUri();

        return uri.toString();
    }

    //    @GetMapping()
//    ResponseEntity getImage(@RequestParam("id") Long id,UriComponentsBuilder ur) {
//        var image = imageService.getImage(id);
//
//        return ResponseEntity.status(HttpStatus.OK)
//                .contentType(MediaType.valueOf("image/png"))
//                .body(image);
//
//    }
    @GetMapping()
    ResponseEntity getImages(@RequestParam("url") String url, UriComponentsBuilder ur) {
        var image = imageService.getImages(url, ur);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(image);

    }
}

