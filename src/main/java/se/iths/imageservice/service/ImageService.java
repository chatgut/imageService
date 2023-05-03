package se.iths.imageservice.service;

import org.springframework.stereotype.Service;
import se.iths.imageservice.repository.ImageRepository;

@Service
public class ImageService {

    ImageRepository repo;

    public ImageService(ImageRepository repo) {
        this.repo = repo;
    }


    public String uploadImage(){

        return "";
    }
}
