package com.simprints.id.controllers;

import android.support.annotation.NonNull;

import com.simprints.id.domain.ALERT_TYPE;

public interface SetupCallback {

    void onSuccess();

    void onProgress(int progress, int detailsId);

    void onError(int resultCode);

    void onAlert(@NonNull ALERT_TYPE alertType);

}
