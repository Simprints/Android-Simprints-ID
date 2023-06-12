package com.simprints.fingerprint.infra.scanner.v1;

public interface ScannerCallback {

    void onSuccess();

    void onFailure(SCANNER_ERROR error);

}
