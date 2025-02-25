package dev.easycloud.service.loader;

import dev.vankka.dependencydownload.classpath.ClasspathAppender;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public final class ClassPathLoader extends URLClassLoader implements ClasspathAppender {

    public ClassPathLoader() {
        super(new URL[0], ClassLoader.getSystemClassLoader());
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    @SneakyThrows
    public void appendFileToClasspath(@NotNull Path path) {
        this.addURL(path.toAbsolutePath().toUri().toURL());
    }
}
