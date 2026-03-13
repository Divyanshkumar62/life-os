package com.lifeos.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("FirebaseApp already initialized");
                return;
            }

            // Try environment variable first
            String envPath = System.getenv("FIREBASE_SERVICE_ACCOUNT");
            Path serviceAccountPath = null;

            if (envPath != null && !envPath.isBlank()) {
                serviceAccountPath = Path.of(envPath);
            } else {
                // Fallback to docs/serviceAccountKey.json in workspace root
                Path defaultPath = Path.of("docs", "serviceAccountKey.json");
                if (Files.exists(defaultPath)) {
                    serviceAccountPath = defaultPath.toAbsolutePath();
                } else {
                    // look for any json file matching *firebase-adminsdk* within docs
                    try (var stream = Files.list(Path.of("docs"))) {
                        serviceAccountPath = stream
                            .filter(p -> p.getFileName().toString().toLowerCase().contains("firebase-adminsdk")
                                    && p.getFileName().toString().toLowerCase().endsWith(".json"))
                            .findFirst()
                            .map(Path::toAbsolutePath)
                            .orElse(null);
                    } catch (Exception e) {
                        // ignore, we'll handle missing path below
                        serviceAccountPath = null;
                    }
                }
            }

            if (serviceAccountPath == null || !Files.exists(serviceAccountPath)) {
                log.warn("No Firebase service account file found. Firebase will not be initialized. " +
                        "Place a serviceAccountKey.json (or any *firebase-adminsdk*.json) in docs/ " +
                        "or set FIREBASE_SERVICE_ACCOUNT env var to its path.");
                return;
            }

            try (InputStream serviceAccount = new FileInputStream(serviceAccountPath.toFile())) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Initialized FirebaseApp with service account: {}", serviceAccountPath);
            }

        } catch (Exception e) {
            log.error("Failed to initialize FirebaseApp: {}", e.getMessage(), e);
        }
    }
}
