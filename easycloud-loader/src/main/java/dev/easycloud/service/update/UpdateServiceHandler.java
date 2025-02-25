package dev.easycloud.service.update;

import dev.easycloud.service.terminal.SimpleTerminal;

public class UpdateServiceHandler {

    public UpdateServiceHandler() {
        SimpleTerminal.clear();
        System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │      Checking for update...      │
                  │                                  │
                  └──────────────────────────────────┘
                """);
    }
}