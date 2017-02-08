package com.simprints.id.tools;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.simprints.id.BuildConfig;

import java.util.HashMap;

public class RemoteConfig {
    private static HashMap<String, Object> configMap = new HashMap<>();

    public final static String ENABLE_EMPTY_USER_ID = "enable_empty_user_id";
    public final static String ENABLE_RETURNING_TEMPLATES = "enable_returning_templates";
    public final static String ENABLE_REFUSAL_FORMS = "enable_refusal_forms";
    public final static String ENABLE_CCDBR_ON_LOADING = "enable_ccdbr_on_loading";

    private static HashMap<String, Object> getDefaults() {
        configMap.put(ENABLE_EMPTY_USER_ID, true);
        configMap.put(ENABLE_RETURNING_TEMPLATES, false);
        configMap.put(ENABLE_REFUSAL_FORMS, true);
        configMap.put(ENABLE_CCDBR_ON_LOADING, false);

        return configMap;
    }

    public static void init() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        remoteConfig.setConfigSettings(configSettings);
        remoteConfig.setDefaults(getDefaults());

        long cacheExp = 18000; // 5 hour in seconds.
        if (remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExp = 0;
        }

        remoteConfig.fetch(cacheExp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
            }
        });
    }

    public static FirebaseRemoteConfig get() {
        return FirebaseRemoteConfig.getInstance();
    }

}
