package com.simprints.fingerprintscanner;

public interface ScannerCallback {

    void onSuccess();

    void onFailure(SCANNER_ERROR error);

}
