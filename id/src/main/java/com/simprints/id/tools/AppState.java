package com.simprints.id.tools;

import com.simprints.libscanner.Scanner;


@SuppressWarnings("ConstantConditions")
public class AppState {

    private static AppState singleton;

    public synchronized static AppState getInstance() {
        if (singleton == null) {
            singleton = new AppState();
        }
        return singleton;
    }

    private Scanner scanner = null;

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void destroy() {
        singleton = null;
    }

}
