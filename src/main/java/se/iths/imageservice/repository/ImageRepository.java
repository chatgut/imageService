package se.iths.imageservice.repository;

import org.springframework.data.repository.ListCrudRepository;
import se.iths.imageservice.entities.ImageEntity;

public interface ImageRepository extends ListCrudRepository<ImageEntity, Long> {
    ImageEntity findByName(String name);
}
