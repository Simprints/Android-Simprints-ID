package com.simprints.id.controllers;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.simprints.id.model.ALERT_TYPE;

public interface SetupCallback {

    void onSuccess();

    void onProgress(int progress, int detailsId);

    void onError(int resultCode, Intent resultData);

    void onAlert(@NonNull ALERT_TYPE alertType);

}
