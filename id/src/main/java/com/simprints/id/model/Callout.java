package com.simprints.id.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.simprints.libsimprints.Constants;

import java.util.HashMap;
import java.util.Map;

public enum Callout {
    REGISTER(Constants.SIMPRINTS_REGISTER_INTENT),
    IDENTIFY(Constants.SIMPRINTS_IDENTIFY_INTENT),
    UPDATE(Constants.SIMPRINTS_UPDATE_INTENT),
    VERIFY(Constants.SIMPRINTS_VERIFY_INTENT);

    private final static Map<String, Callout> actionCalloutMapping;

    static {
        actionCalloutMapping = new HashMap<>();
        for (Callout callout : Callout.values())
            actionCalloutMapping.put(callout.action, callout);
    }

    @NonNull
    public static String toString(@Nullable Callout callout) {
        if (callout == null)
            return "";
        else
            return callout.toString();
    }

    @Nullable
    public static Callout fromAction(@Nullable String action) {
        return actionCalloutMapping.get(action);
    }

    private String action;

    Callout(String action) {
        this.action = action;
    }
}
