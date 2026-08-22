package com.commanderanthology.desktop;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CardImageCacheSmokeTest {
    private CardImageCacheSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-image-cache-test");
        CardImageCache cache = new CardImageCache(tempDir);
        String id = "c4300d24-1cae-4dd5-be7e-38cc677cf5bd";
        require(cache.cachePath(id).startsWith(tempDir), "cache path root");
        require(cache.cachePath("bad/id:*").getFileName().toString().equals("bad_id__.jpg"), "safe file name");
        require(!cache.hasCachedImage(id), "not cached before write");
        Files.writeString(cache.cachePath(id), "not actually an image");
        require(cache.hasCachedImage(id), "cached after write");
        System.out.println("Card image cache smoke test passed: " + tempDir);
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
