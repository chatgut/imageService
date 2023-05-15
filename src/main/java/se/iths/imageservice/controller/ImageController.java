package se.iths.imageservice.controller;

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

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @PostMapping()
    String uploadImage(@RequestParam("image") MultipartFile image, UriComponentsBuilder ur) {
        return ur.path("/images/" + imageService.uploadImage(image))
                .build()
                .toUri()
                .toString();
    }


    @GetMapping("/{id}")
    ResponseEntity getImages(@PathVariable Long id) {
        return imageService.getImg(id);
    }

    @GetMapping("/thumbnail/{id}")
    ResponseEntity getThumbnail(@PathVariable Long id,@RequestParam("height") int height, @RequestParam("width") int width){
        return imageService.getThumbnail(id, height, width);
    }
}

