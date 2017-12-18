package com.simprints.libdata;

@SuppressWarnings("unused")
public enum DATA_ERROR {

    INVALID_API_KEY("API key auth failed"),
    UNVERIFIED_API_KEY("API key could not be checked because the server is unreachable"),
    DATABASE_INIT_RESTART("Requesting the sessions to restart"),
    SYNC_INTERRUPTED("Syncing was interrupted because connection was lost"),
    NOT_FOUND("The requested element was not found in the database"),

    FAILED_TO_UPLOAD("Failed to upload to online storage"),
    JSON_ERROR("Could not convert database into JSON format."),
    IO_BUFFER_WRITE_ERROR("Could not write to output stream.");

    private String details;

    DATA_ERROR(String details) {
        this.details = details;
    }

    public String details() {
        return details;
    }

}
