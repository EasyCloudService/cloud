package dev.easycloud.service.terminal.completer;

import dev.easycloud.service.EasyCloudCluster;
import lombok.Getter;
import lombok.Setter;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

public final class TerminalCompleter implements Completer {
    @Getter
    private static List<String> TEMP_VALUES = new ArrayList<>();

    @Getter
    @Setter
    private static boolean enabled = true;

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        if (parsedLine.line().startsWith(" ") || !enabled) return;

        if(!TEMP_VALUES.isEmpty()) {
            TEMP_VALUES.forEach(it -> list.add(new Candidate(it)));
            return;
        }

        if(EasyCloudCluster.instance().terminal().readingThread().priority() != null) return;

        var args = parsedLine.line().split(" ", -1);
        if (args.length >= 2) {
            var command = EasyCloudCluster.instance().commandProvider()
                    .commands()
                    .stream().filter(it -> it.name().equals(args[0]) || it.aliases().stream().anyMatch(it2 -> it2.equalsIgnoreCase(args[0])))
                    .findFirst()
                    .orElse(null);
            if (command == null) return;

            if(args.length >= 3) {
                var subCommand = command.commandNodes()
                        .stream().filter(it -> it.name().equalsIgnoreCase(args[args.length - 2]))
                        .findFirst()
                        .orElse(null);
                if (subCommand == null) return;

                subCommand.commandNodes().forEach(it -> list.add(new Candidate(it.name(), it.name(), null, it.description(), null, null, true)));
                return;
            }

            command.commandNodes().forEach(it -> list.add(new Candidate(it.name(), it.name(), null, it.description(), null, null, true)));
            return;
        }

        EasyCloudCluster.instance().commandProvider()
                .commands()
                .forEach(it -> {
                    list.add(new Candidate(it.name(), it.name(), null, it.description(), null, null, true));
                    it.aliases().forEach(it2 -> list.add(new Candidate(it2, it2, null, it.description(), null, null, true)));
                });
    }
}
