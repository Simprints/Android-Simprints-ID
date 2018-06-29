package com.simprints.id.data.db.remote.enums;


public enum REFUSAL_FORM_REASON {
    SCANNER_NOT_HERE("scanner_not_here"),
    SCANNER_NOT_WORKING("scanner_not_working"),
    UNABLE_TO_CAPTURE_GOOD_SCAN("unable_to_capture_good_scan"),
    UNABLE_TO_GIVE_PRINTS("unable_to_give_prints"),
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
