package com.simprints.id.data.db.remote.enums;

@Deprecated
public enum REFUSAL_FORM_REASON {
    SCANNER_NOT_WORKING("scanner_not_working"),
    REFUSED("refused"),
    OTHER("other");

    private String title;

    REFUSAL_FORM_REASON(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
