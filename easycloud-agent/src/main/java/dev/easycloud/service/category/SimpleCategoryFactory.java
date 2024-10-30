package dev.easycloud.service.category;

import dev.easycloud.service.category.resources.Category;
import dev.easycloud.service.file.FileFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SimpleCategoryFactory implements CategoryFactory {
    private final List<Category> categories;

    private final Path CATEGORIES_PATH = Path.of("storage").resolve("categories");

    public SimpleCategoryFactory() {
        this.categories = new ArrayList<>();

        var pathFile = this.CATEGORIES_PATH.toFile();
        if(!pathFile.exists()) {
            pathFile.mkdirs();
        }

        for (File file : pathFile.listFiles()) {
            this.categories.add(FileFactory.readRaw(file.toPath(), Category.class));
        }
    }

    @Override
    public void create(Category group) {
        this.categories.add(group);
        FileFactory.writeRaw(this.CATEGORIES_PATH.resolve(group.name() + ".json"), group);
    }

    @Override
    public Category get(String name) {
        return this.categories.stream()
                .filter(it -> it.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Category> categories() {
        return this.categories;
    }
}
