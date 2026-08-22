package com.commanderanthology.desktop;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

final class CardImageCache {
    private final Path cacheDirectory;

    CardImageCache() {
        this(defaultCacheDirectory());
    }

    CardImageCache(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    Path cacheDirectory() {
        return cacheDirectory;
    }

    BufferedImage load(String scryfallCardId, String imageUrl) throws IOException {
        if (scryfallCardId == null || scryfallCardId.isBlank() || imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        Files.createDirectories(cacheDirectory);
        Path target = cachePath(scryfallCardId);
        if (Files.exists(target)) {
            return ImageIO.read(target.toFile());
        }
        BufferedImage image = ImageIO.read(URI.create(imageUrl).toURL());
        if (image != null) {
            ImageIO.write(image, "jpg", target.toFile());
        }
        return image;
    }

    Path cachePath(String scryfallCardId) {
        String clean = scryfallCardId.replaceAll("[^A-Za-z0-9_-]", "_");
        return cacheDirectory.resolve(clean + ".jpg");
    }

    boolean hasCachedImage(String scryfallCardId) {
        return Files.exists(cachePath(scryfallCardId));
    }

    private static Path defaultCacheDirectory() {
        String appData = System.getenv("APPDATA");
        Path root = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), "AppData", "Roaming")
                : Path.of(appData);
        return root.resolve("Commander Anthology").resolve("card-images");
    }
}
