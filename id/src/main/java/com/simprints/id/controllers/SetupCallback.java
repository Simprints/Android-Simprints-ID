package com.simprints.id.controllers;

import android.support.annotation.Nullable;

import com.simprints.id.model.ALERT_TYPE;

public interface SetupCallback {

    void onSuccess();

    void onProgress(int progress, @Nullable String details);

    void onError(ALERT_TYPE alertType);

}
