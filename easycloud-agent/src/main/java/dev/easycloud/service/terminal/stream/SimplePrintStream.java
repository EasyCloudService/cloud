package dev.easycloud.service.terminal.stream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class SimplePrintStream extends PrintStream {

    public SimplePrintStream(OutputStream out) {
        super(out, true, StandardCharsets.UTF_8);
    }

}
