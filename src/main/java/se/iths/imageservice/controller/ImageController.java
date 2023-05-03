package se.iths.imageservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/images")
public class ImageController {

@GetMapping
    String getPathToImage(){
    return "";
}
}

