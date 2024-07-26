package dev.easycloud.service.terminal.stream;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class SimplePrintStream extends PrintStream {

    public SimplePrintStream(OutputStream out) {
        super(out, true, StandardCharsets.UTF_8);
    }
}
