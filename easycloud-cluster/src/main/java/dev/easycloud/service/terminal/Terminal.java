package dev.easycloud.service.terminal;

import dev.easycloud.service.service.Service;

public interface Terminal {
    TerminalState state();
    boolean logging();

    void exit(Service service);

}
