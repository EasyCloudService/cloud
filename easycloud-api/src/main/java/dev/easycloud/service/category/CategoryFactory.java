package dev.easycloud.service.category;

import dev.easycloud.service.category.resources.Category;

public interface CategoryFactory {
    void create(Category group);
    Category get(String name);
}
