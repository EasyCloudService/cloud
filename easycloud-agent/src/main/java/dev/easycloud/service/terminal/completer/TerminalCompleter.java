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
                it.alias().forEach(it2 -> list.add(new Candidate(it2)));
            });
            return;
        }
        EasyCloudAgent.instance().commandHandler().commands().stream()
                .filter(it -> it.name().toLowerCase().startsWith(parsedLine.line().toLowerCase()))
                .forEach(it -> list.add(new Candidate(it.name())));

        for (Command command : EasyCloudAgent.instance().commandHandler().commands()) {
            command.alias().stream()
                    .filter(it -> it.toLowerCase().startsWith(parsedLine.line().toLowerCase()))
                    .forEach(it -> list.add(new Candidate(it)));
        }
    }
}
