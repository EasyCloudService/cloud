package dev.easycloud.service.terminal;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@UtilityClass
public final class SimpleTerminal {
    private  final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void print(String message) {
        System.out.println("[" + DATE_FORMAT.format(Calendar.getInstance().getTime()) + "] INFO: " + message);
    }
}
