package com.simprints.fingerprintscanner.v1;

public interface ScannerCallback {

    void onSuccess();

    void onFailure(SCANNER_ERROR error);

}
