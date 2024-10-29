package dev.easycloud.service.category;

import dev.easycloud.service.category.resources.Category;
import dev.httpmarco.evelon.Repository;
import dev.httpmarco.evelon.sql.h2.H2Layer;

import java.util.List;

public final class SimpleCategoryFactory implements CategoryFactory {
    private final Repository<Category> repository = Repository.build(Category.class).withLayer(H2Layer.class).build();

    @Override
    public void create(Category group) {
        this.repository.query().create(group);
    }

    @Override
    public Category get(String name) {
        return this.repository.query().match("name", name).findFirst();
    }

    @Override
    public List<Category> categories() {
        return this.repository.query().find();
    }
}
