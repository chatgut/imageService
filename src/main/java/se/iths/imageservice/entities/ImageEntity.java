package se.iths.imageservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String filePath;
    private String type;
    private String thumbnail;

    @Override
    public String toString() {
        return "ImageEntity{" +
                "filePath='" + filePath + '\'' +
                '}';
    }
}
