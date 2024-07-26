package dev.easycloud.service.terminal.highlighter;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;

import java.util.regex.Pattern;

public final class TerminalHighlighter implements Highlighter {

    @Override
    public AttributedString highlight(LineReader lineReader, String s) {
        return new AttributedString(s);
    }

    @Override
    public void setErrorPattern(Pattern pattern) {

    }

    @Override
    public void setErrorIndex(int i) {

    }
}
