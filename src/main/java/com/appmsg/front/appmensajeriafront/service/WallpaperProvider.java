package com.appmsg.front.appmensajeriafront.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class WallpaperProvider {
    private static final String RESOURCE_DIR = "com/appmsg/front/appmensajeriafront/wallpapers";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    public static List<String> getWallpaperUrls() {
        Set<String> urls = new HashSet<>();
        discoverFromDirectory(resolveWritableWallpaperDirectory(), urls);
        if (urls.isEmpty()) {
            discoverFromClasspath(urls);
        }

        List<String> sortedUrls = new ArrayList<>(urls);
        Collections.sort(sortedUrls);
        return sortedUrls;
    }

    public static String importWallpaper(File selectedFile) throws IOException {
        if (selectedFile == null || !selectedFile.isFile()) {
            throw new IOException("Archivo de wallpaper no valido");
        }

        Path source = selectedFile.toPath();
        String extension = getExtension(source.getFileName().toString());
        if (!isAllowedExtension(extension)) {
            throw new IOException("Formato de imagen no soportado");
        }

        Path wallpaperDirectory = resolveWritableWallpaperDirectory();
        Files.createDirectories(wallpaperDirectory);

        if (source.toAbsolutePath().normalize().getParent() != null
                && source.toAbsolutePath().normalize().getParent().equals(wallpaperDirectory.toAbsolutePath().normalize())) {
            return source.toUri().toString();
        }

        Path target = buildUniqueTarget(wallpaperDirectory, source.getFileName().toString(), extension);
        if (!source.toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())) {
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        }

        return target.toUri().toString();
    }

    private static Path resolveWritableWallpaperDirectory() {
        Path projectDirectory = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", RESOURCE_DIR);
        if (Files.isDirectory(projectDirectory) || Files.isDirectory(projectDirectory.getParent())) {
            return projectDirectory;
        }

        URL resourceUrl = WallpaperProvider.class.getResource("/" + RESOURCE_DIR);
        if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
            try {
                return Paths.get(resourceUrl.toURI());
            } catch (Exception ignored) {
            }
        }

        return Paths.get(System.getProperty("user.home"), ".appmensajeriafront", "wallpapers");
    }

    private static void discoverFromDirectory(Path directory, Set<String> urls) {
        if (directory == null || !Files.isDirectory(directory)) return;

        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isAllowedExtension(getExtension(path.getFileName().toString())))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .forEach(path -> urls.add(path.toUri().toString()));
        } catch (IOException ignored) {
        }
    }

    private static void discoverFromClasspath(Set<String> urls) {
        URL resourceUrl = WallpaperProvider.class.getResource("/" + RESOURCE_DIR);
        if (resourceUrl == null) return;

        if ("file".equals(resourceUrl.getProtocol())) {
            try {
                discoverFromDirectory(Paths.get(resourceUrl.toURI()), urls);
            } catch (Exception ignored) {
            }
            return;
        }

        if (!"jar".equals(resourceUrl.getProtocol())) return;

        try {
            URI uri = resourceUrl.toURI();
            String[] parts = uri.toString().split("!", 2);
            try (FileSystem fs = FileSystems.newFileSystem(URI.create(parts[0]), Collections.emptyMap())) {
                discoverFromDirectory(fs.getPath("/" + RESOURCE_DIR), urls);
            }
        } catch (Exception ignored) {
        }
    }

    private static Path buildUniqueTarget(Path directory, String originalName, String extension) {
        String baseName = originalName;
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            baseName = originalName.substring(0, dot);
        }

        baseName = baseName.replaceAll("[^A-Za-z0-9._-]+", "_");
        if (baseName.isBlank()) {
            baseName = "wallpaper";
        }

        Path candidate = directory.resolve(baseName + "." + extension);
        int index = 1;
        while (Files.exists(candidate)) {
            candidate = directory.resolve(baseName + "-" + index + "." + extension);
            index++;
        }
        return candidate;
    }

    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean isAllowedExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}
