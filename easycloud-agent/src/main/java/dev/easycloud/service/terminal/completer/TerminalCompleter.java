package dev.easycloud.service.terminal.completer;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public final class TerminalCompleter implements Completer {

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {


        if (parsedLine.line().isEmpty()) {
            EasyCloudAgent.instance().commandHandler().commands().forEach(it -> {
                list.add(new Candidate(it.name()));
                it.aliases().forEach(it2 -> list.add(new Candidate(it2)));
            });
            return;
        }
        if(parsedLine.line().startsWith(" ")) return;

        var args = parsedLine.line().split(" ", -1);
        if(args.length > 2) {
            list.add(new Candidate(""));
            return;
        }

        if(args.length == 2) {
            var command = EasyCloudAgent.instance().commandHandler()
                    .commands()
                    .stream().filter(it -> it.name().equals(args[0]) || it.aliases().stream().anyMatch(it2 -> it2.equalsIgnoreCase(args[0])))
                    .findFirst()
                    .orElse(null);
            if(command == null) return;

            if(args[1].isEmpty()) {
                command.subCommands().forEach((it, unused) -> list.add(new Candidate(it)));
                return;
            }

            command.subCommands()
                    .entrySet()
                    .stream()
                    .filter(it -> it.getKey().startsWith(args[0]))
                    .forEach(it -> list.add(new Candidate(it.getKey())));

            return;
        }

        EasyCloudAgent.instance().commandHandler().commands().stream()
                .filter(it -> it.name().toLowerCase().startsWith(parsedLine.line().toLowerCase()))
                .forEach(it -> list.add(new Candidate(it.name())));

        for (Command command : EasyCloudAgent.instance().commandHandler().commands()) {
            command.aliases().stream()
                    .filter(it -> it.toLowerCase().startsWith(parsedLine.line().toLowerCase()))
                    .forEach(it -> list.add(new Candidate(it)));
        }
    }
}
