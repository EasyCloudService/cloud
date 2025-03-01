package dev.easycloud.service.terminal.stream;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Log4j2Stream. Thanks to: https://github.com/HttpMarco
 */
@AllArgsConstructor
public final class SimpleLoggingStream extends ByteArrayOutputStream {
    private final Consumer<String> callback;

    @Override
    @SneakyThrows
    public void flush() {
        super.flush();

        var input = this.toString(StandardCharsets.UTF_8);
        super.reset();

        if (input != null && !input.isEmpty()) {
            callback.accept(input.replace("\n", ""));
        }
    }

    @Contract(" -> new")
    public @NotNull PrintStream printStream() {
        return new PrintStream(this, true, StandardCharsets.UTF_8);
    }
}