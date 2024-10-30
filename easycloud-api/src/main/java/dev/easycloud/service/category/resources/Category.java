package dev.easycloud.service.category.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Category {
    private final String name;
    private final CategoryType type;

    private int memory;
}
