package com.simprints.libdata.models.enums;


public enum VERIFY_GUID_EXISTS_RESULT {
    GUID_FOUND("GUID_found"),
    GUID_NOT_FOUND_ONLINE("GUID_not_found_online"),
    GUID_NOT_FOUND_OFFLINE("GUID_not_found_offline"),
    GUID_NOT_FOUND_UNKNOWN("GUID_not_found_unknown_connectivity)");


    private String title;

    VERIFY_GUID_EXISTS_RESULT(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return title;
    }
}
