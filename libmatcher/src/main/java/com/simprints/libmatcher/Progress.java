package com.simprints.libmatcher;

import java.util.Locale;

@SuppressWarnings("unused")
public class Progress {

    private int progress;

    public Progress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public String details() {
        return String.format(Locale.UK, "Progress: %d%%", progress);
    }
}
