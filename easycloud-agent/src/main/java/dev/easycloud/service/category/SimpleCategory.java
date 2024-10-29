package dev.easycloud.service.category;

import dev.easycloud.service.category.resources.Category;
import dev.easycloud.service.category.resources.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class SimpleCategory implements Category {
    private final String name;
    private final int memory;
    private final CategoryType type;

}
