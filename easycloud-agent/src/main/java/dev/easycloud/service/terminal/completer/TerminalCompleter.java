package dev.easycloud.service.terminal.completer;

import dev.easycloud.service.EasyCloudAgent;
import lombok.Getter;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

public final class TerminalCompleter implements Completer {
    @Getter
    private static List<String> TEMP_VALUES = new ArrayList<>();

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        if (parsedLine.line().startsWith(" ")) return;

        if(!TEMP_VALUES.isEmpty()) {
            TEMP_VALUES.forEach(it -> list.add(new Candidate(it)));
            return;
        }

        if(EasyCloudAgent.instance().terminal().readingThread().prioSub() != null) return;

        var args = parsedLine.line().split(" ", -1);
        if (args.length >= 2) {
            var command = EasyCloudAgent.instance().commandHandler()
                    .commands()
                    .stream().filter(it -> it.name().equals(args[0]) || it.aliases().stream().anyMatch(it2 -> it2.equalsIgnoreCase(args[0])))
                    .findFirst()
                    .orElse(null);
            if (command == null) return;

            if(args.length >= 3) {
                var subCommand = command.subCommands()
                        .stream().filter(it -> it.name().equalsIgnoreCase(args[args.length - 2]))
                        .findFirst()
                        .orElse(null);
                if (subCommand == null) return;

                subCommand.subCommands().forEach(it -> list.add(new Candidate(it.name(), it.name(), null, it.description(), null, null, true)));
                return;
            }

            command.subCommands().forEach(it -> list.add(new Candidate(it.name(), it.name(), null, it.description(), null, null, true)));
            return;
        }

        EasyCloudAgent.instance().commandHandler()
                .commands()
                .forEach(it -> list.add(new Candidate(it.name(), it.name(), null, it.description(), null, null, true)));
    }
}
