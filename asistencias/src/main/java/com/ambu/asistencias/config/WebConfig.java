package com.ambu.asistencias.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * WebConfig - Web MVC configuration
 * - Serves uploaded photos as static resources
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:uploads/photos}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String uploadLocation = "file:///" + uploadPath.toString().replace("\\", "/") + "/";

            // Map /uploads/photos/** to the physical upload directory
            registry.addResourceHandler("/uploads/photos/**")
                    .addResourceLocations(uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
}