package dev.easycloud.service.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.easycloud.service.file.resources.FileEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
public final class FileFactory {
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    public static void write(Path path, Object object) {
        var file = path.resolve(name(object.getClass()));
        if (file.toFile().exists()) {
            file.toFile().delete();
        }

        try (FileWriter writer = new FileWriter(file.toFile().getPath())) {
            GSON.toJson(object, writer);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T read(Path path, Class<T> clazz) {
        var file = path.resolve(name(clazz));
        try (Reader reader = new FileReader(file.toFile().getPath())) {
            return GSON.fromJson(reader, clazz);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void removeDirectory(Path path) {
        try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
        }
    }
}
