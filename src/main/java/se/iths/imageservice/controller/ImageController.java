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

    //TODO handle exceptions
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }


    @PostMapping()
    String uploadImage(@RequestParam("image") MultipartFile file, UriComponentsBuilder ur) {
        var id = imageService.uploadImage(file);

        return ur.path("/images/" + id).build().toUri().toString();
    }


    @GetMapping("/{id}")
    ResponseEntity getImages(@PathVariable Long id) {
        return imageService.getImg(id);
    }
}

