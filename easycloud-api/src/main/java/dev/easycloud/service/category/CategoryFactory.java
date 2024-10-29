package dev.easycloud.service.category;

import dev.easycloud.service.category.resources.Category;

import java.util.List;

public interface CategoryFactory {
    void create(Category group);
    Category get(String name);
    List<Category> categories();
}
