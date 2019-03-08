package com.simprints.libscanner;

public interface ScannerCallback {

    void onSuccess();

    void onFailure(SCANNER_ERROR error);

}