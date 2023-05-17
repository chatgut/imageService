package se.iths.imageservice.config;

import org.springframework.stereotype.Service;

@Service
public class ApplicationDto {
    private String runner = System.getProperty("spring.profiles.active", "main");
    private String mode;

    public ApplicationDto() {
        if (runner.equals("local"))
            mode = System.getProperty("user.home");
        else
            mode = "";
    }

    public String getMode() {
        return mode;
    }
}
