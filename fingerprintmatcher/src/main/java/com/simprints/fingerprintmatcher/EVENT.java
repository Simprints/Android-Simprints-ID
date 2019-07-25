package com.simprints.fingerprintmatcher;

public enum EVENT {

    MATCH_COMPLETED("All the matching scores were computed successfully"),
    MATCH_CANCELLED("Computation of the matching scores cancelled"),
    MATCH_ALREADY_RUNNING("Computation of the matching scores already in progress"),
    MATCH_NOT_RUNNING("Computation of the matching scores cannot be cancelled as it has not started or is over");

    private String details;

    EVENT(String details) {
        this.details = details;
    }

    public String details() {
        return details;
    }
}
