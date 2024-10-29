package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.category.SimpleCategory;
import dev.easycloud.service.category.resources.Category;
import dev.easycloud.service.category.resources.CategoryType;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.sub.SubCommand;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.logger.LoggerColor;
import dev.easycloud.service.terminal.logger.SimpleLogger;

import java.util.Arrays;

public final class CategoryCommand extends Command {
    public CategoryCommand() {
        super("category", "Manage categories.", "cg");

        addSubCommand(new SubCommand("list", "List all categories.", this::list));
        addSubCommand(new SubCommand("setup", "Start category setup.", this::setup));
    }

    @Override
    public void executeBase() {
        SimpleLogger.error("Wrong usage.");
        SimpleLogger.info("category [list]");
        SimpleLogger.info("category [setup]");
        SimpleLogger.info("category [delete] [name]");

    }

    private void list(String[] args) {
        EasyCloudAgent.instance().categoryFactory().categories().forEach(it -> {
            SimpleLogger.info("Name: " + it.name());
            SimpleLogger.info("Memory: " + it.memory());
            SimpleLogger.info("Type: " + it.type());
        });
    }

    private void setup(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("name", "What should the name be?", null))
                .add(new SetupData<>("platform", "What should the platform be?", Arrays.stream(LoggerColor.values()).toList()))
                .add(new SetupData<>("memory", "How much memory should the category have?", null))
                .publish()
                .thenAccept(it -> {
                    var category = new SimpleCategory(it.result("name", String.class), it.result("memory", Integer.class), CategoryType.SERVER);
                    EasyCloudAgent.instance().categoryFactory().create(category);
                });
    }
}
