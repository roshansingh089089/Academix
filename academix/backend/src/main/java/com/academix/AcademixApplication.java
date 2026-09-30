package com.academix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.nio.file.*;

@SpringBootApplication
public class AcademixApplication {
    public static void main(String[] args) {
        createDatabaseDirectory();
        SpringApplication.run(AcademixApplication.class, args);
    }

    static void createDatabaseDirectory() {
        String configured = System.getenv().getOrDefault("ACADEMIX_DB_PATH", "../data/academix.db");
        Path database = Path.of(configured).toAbsolutePath().normalize();
        Path parent = database.getParent();
        if (parent == null) return;
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create SQLite data directory: " + parent, exception);
        }
    }
}
