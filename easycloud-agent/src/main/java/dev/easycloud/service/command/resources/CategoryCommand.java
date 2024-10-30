package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.category.resources.Category;
import dev.easycloud.service.category.resources.CategoryType;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.sub.SubCommand;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.LogType;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

import static org.fusesource.jansi.Ansi.ansi;

@Log4j2
public final class CategoryCommand extends Command {
    public CategoryCommand() {
        super("category", "Manage categories.", "cg");

        addSubCommand(new SubCommand("list", "List all categories.", this::list));
        addSubCommand(new SubCommand("setup", "Start category setup.", this::setup));
    }

    @Override
    public void executeBase() {
        log.error("Wrong usage.");
        log.info("category [list]");
        log.info("category [setup]");
        log.info("category [delete] [name]");

    }

    private void list(String[] args) {
        var categories = EasyCloudAgent.instance().categoryFactory().categories();
        if(categories.isEmpty()) {
            log.error("No categories found.");
            return;
        }

        categories.forEach(it -> {
            log.info("");
            log.info(ansi().fgRgb(LogType.PRIMARY.rgb()).a(it.name()).reset());
            log.info("* Memory: {}", it.memory());
            log.info("* Type: {}", it.type());
        });
        log.info("");
    }

    private void setup(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("name", "What should the name be?", null))
                .add(new SetupData<>("platform", "What should the platform be?", Arrays.stream(LogType.values()).toList()))
                .add(new SetupData<>("memory", "How much memory should the category have?", null))
                .publish()
                .thenAccept(it -> {
                    var category = new Category(it.result("name", String.class), CategoryType.SERVER, it.result("memory", Integer.class));
                    EasyCloudAgent.instance().categoryFactory().create(category);

                    log.info("Category created with name: {}", category.name());
                });
    }
}
