package dev.easycloud.service.command.resources;

import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.sub.SubCommand;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.logger.LoggerColor;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.SneakyThrows;

import java.util.Arrays;

public final class CategoryCommand extends Command {
    public CategoryCommand() {
        super("category", "Manage categories.", "cg");

        addSubCommand(new SubCommand("setup", "Start category setup.", this::setup));
    }

    @Override
    @SneakyThrows
    public void executeBase() {
        SimpleLogger.error("Wrong usage.");
        SimpleLogger.info("category [setup]");
        SimpleLogger.info("category [delete] [name]");

    }

    private void setup(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("categories", "Whats should the name be?", null))
                .add(new SetupData<>("test", "Andere frage", Arrays.stream(LoggerColor.values()).toList()))
                .publish()
                .thenAccept(it -> {
                    SimpleLogger.info("RESULT1: " + it.result("categories", String.class));
                    SimpleLogger.info("RESULT2: " + it.result("test", LoggerColor.class));
                });
    }
}
