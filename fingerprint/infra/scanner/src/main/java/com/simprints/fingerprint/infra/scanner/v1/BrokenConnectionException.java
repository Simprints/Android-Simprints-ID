package com.simprints.fingerprint.infra.scanner.v1;

class BrokenConnectionException extends Exception {

    BrokenConnectionException(String message) {
        super(message);
    }

}
