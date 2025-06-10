package dev.easycloud.service.terminal.completer;

import dev.easycloud.service.EasyCloudCluster;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.*;

@Slf4j
@Getter
public final class TerminalCompleter implements Completer {
    @Setter
    private boolean enabled = true;
    private final List<Candidate> possibleResults = new ArrayList<>();

    @Override
    public void complete(LineReader reader, ParsedLine parsedLine, List<Candidate> candidates) {
        if (!this.enabled) {
            return;
        }

        var line = parsedLine.line();
        List<Candidate> possibleResults = new ArrayList<>();
        if (!this.possibleResults.isEmpty()) {
            possibleResults.addAll(this.possibleResults);
        }

        var splitLine = line.split(" ", -1);
        var lineCount = splitLine.length;
        if (lineCount >= 4) {
            return;
        }

        if (possibleResults.isEmpty()) {
            if (lineCount > 1) {
                var command = EasyCloudCluster.instance().commandProvider().commands().stream().filter(it -> it.name().equalsIgnoreCase(splitLine[0])).findFirst().orElse(null);
                if (command == null) {
                    return;
                }
                var node = command.commandNodes().stream()
                        .filter(it -> it.name().equalsIgnoreCase(splitLine[1]))
                        .findFirst()
                        .orElse(null);

                if (node == null) {
                    command.commandNodes().forEach(it -> {
                        possibleResults.add(new Candidate(it.name(), it.name(), null, null, null, null, true));
                    });
                } else {
                    node.completer().forEach(completer -> {
                        possibleResults.add(new Candidate(completer, completer, null, null, null, null, true));
                    });
                }
            } else {
                EasyCloudCluster.instance().commandProvider().commands().forEach(command -> {
                    possibleResults.add(new Candidate(command.name(), command.name(), null, null, null, null, true));
                });
            }
        }

        if (line.trim().isEmpty()) {
            candidates.addAll(possibleResults);
            return;
        }

        for (Candidate candidate : possibleResults) {
            if (candidate.value().startsWith(splitLine[lineCount - 1])) {
                candidates.add(candidate);
            }
        }
    }
}
