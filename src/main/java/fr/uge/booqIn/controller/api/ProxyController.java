package fr.uge.booqIn.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/api/v1/proxy")
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    private static final List<String> ALLOWED_DOMAINS = List.of(
            "books.google.com",
            "covers.openlibrary.org"
    );

    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            if (!isAllowedUrl(url)) {
                logger.warn("Tentative d'accès à une URL non autorisée: {}", url);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            var uri = new URI(url);
            var imageUrl = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestMethod("GET");

            // User-Agent
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                logger.warn("L'URL demandée a renvoyé un code d'erreur: {} - {}", responseCode, url);
                return ResponseEntity.status(responseCode).build();
            }

            byte[] imageBytes = readAllBytes(connection.getInputStream());

            String contentType = connection.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg"; // Type par défaut
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Cache-Control", "public, max-age=86400") // Cache pendant 24 heures
                    .body(imageBytes);

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'image: {}", url, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws Exception {

        try (inputStream; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }

    private boolean isAllowedUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            URL parsedUrl = URI.create(url).toURL();
            String host = parsedUrl.getHost();

            return ALLOWED_DOMAINS.stream().anyMatch(host::endsWith);

        } catch (Exception e) {
            logger.warn("URL invalide: {}", url, e);
            return false;
        }
    }
}