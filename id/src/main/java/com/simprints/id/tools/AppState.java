package com.simprints.id.tools;

import com.simprints.libscanner.Scanner;


@SuppressWarnings("ConstantConditions")
public class AppState {

    private Scanner scanner = null;

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void destroy() {
        scanner = null;
    }
}
