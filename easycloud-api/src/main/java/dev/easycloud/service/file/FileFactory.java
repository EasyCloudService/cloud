package dev.easycloud.service.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.easycloud.service.file.resources.FileEntity;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
public final class FileFactory {
    public static Yaml YAML;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson GSON_NO_PRETTY = new GsonBuilder().create();

    static {
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
            YAML = new Yaml();
        } catch (Exception exception) {
            YAML = null;
        }
    }

    private static String name(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(FileEntity.class)) {
            throw new RuntimeException("Class " + clazz.getName() + " is not annotated with @FileEntity");
        }
        return clazz.getAnnotation(FileEntity.class).name() + ".json";
    }

    public static void writeAsList(Path path, Object object) {
        var file = path.resolve(name(object.getClass()));
        if (file.toFile().exists()) {
            file.toFile().delete();
        }
        try (FileWriter writer = new FileWriter(file.toFile().getPath())) {
            GSON.toJson(List.of(object), writer);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void writeIfNotExists(Path path, Object object) {
        if (!path.resolve(name(object.getClass())).toFile().exists()) {
            write(path, object);
        }
    }

    public static void writeRaw(Path path, Object object) {
        if (path.toFile().exists()) {
            path.toFile().delete();
        }

        try (FileWriter writer = new FileWriter(path.toFile().getPath())) {
            GSON.toJson(object, writer);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void write(Path path, Object object) {
        writeRaw(path.resolve(name(object.getClass())), object);
    }

    public static <T> T readRaw(Path path, Class<T> clazz) {
        try (Reader reader = new FileReader(path.toFile().getPath())) {
            return GSON.fromJson(reader, clazz);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T read(Path path, Class<T> clazz) {
        return readRaw(path.resolve(name(clazz)), clazz);
    }

    public static void remove(Path path) {
        if(!path.toFile().exists()) {
            return;
        }

        try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
        }
    }

    @SneakyThrows
    public static void download(String url, Path output) {
        var connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != 200) {
            throw new IOException("Download failed: HTTP " + connection.getResponseCode());
        }

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(output.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    @SneakyThrows
    public static void copy(Path source, Path destination) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = destination.resolve(source.relativize(dir));
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.println(exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
