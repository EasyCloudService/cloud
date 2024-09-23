package dev.easycloud.service.category;

import dev.easycloud.service.category.ressources.Category;

public interface CategoryFactory {
    void create(Category group);
    Category get(String name);
}
