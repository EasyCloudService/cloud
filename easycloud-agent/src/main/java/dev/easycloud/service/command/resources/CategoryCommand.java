package dev.easycloud.service.command.resources;

import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.sub.SubCommand;
import dev.easycloud.service.terminal.logger.SimpleLogger;

public final class CategoryCommand extends Command {
    public CategoryCommand() {
        super("category", "Manage categories.", "cg");

        addSubCommand(new SubCommand("setup", "Start category setup.", this::setup));
    }

    @Override
    public void executeBase() {
        SimpleLogger.error("Wrong usage.");
        SimpleLogger.info("category [setup]");
        SimpleLogger.info("category [delete] [name]");
    }

    private void setup(String[] args) {
        SimpleLogger.info("Setting up categories...");
    }

    private void delete(String[] args) {
        SimpleLogger.info("Deleting category...");
    }
}
