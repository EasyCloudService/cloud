package dev.easycloud.service.category;

import dev.easycloud.service.category.ressources.Category;
import dev.httpmarco.evelon.Repository;
import dev.httpmarco.evelon.sql.h2.H2Layer;

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
}
