package dev.easycloud.service.terminal;

import dev.easycloud.service.service.Service;

import java.util.List;

public interface Terminal {
    List<String> history();
    TerminalState state();
    boolean logging();

    void clear();
    void clear(boolean redraw);
    void exit(Service service);

}
