package com.simprints.id.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.simprints.id.Application;
import com.simprints.id.data.DataManager;
import com.simprints.id.data.secure.ApiKeyNotFoundException;
import com.simprints.libsimprints.Constants;

import static com.simprints.libsimprints.Constants.SIMPRINTS_SELECTED_GUID;
import static com.simprints.libsimprints.Constants.SIMPRINTS_SESSION_ID;


public class GuidSelectionService extends IntentService {

    private DataManager dataManager;

    public GuidSelectionService() {
        super("GuidSelectionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();
    }

    private boolean hasValidApiKeyExtra(@NonNull Intent intent) {
        String apiKey = intent.getStringExtra(Constants.SIMPRINTS_API_KEY);

        String dmApiKey;
        try {
            dmApiKey = dataManager.getApiKey();
        } catch (ApiKeyNotFoundException ex) {
            dataManager.logException(ex);
            return false;
        }

        return apiKey != null && apiKey.equals(dmApiKey);
    }

    private boolean hasValidSessionIdExtra(@NonNull Intent intent) {
        String sessionId = intent.getStringExtra(SIMPRINTS_SESSION_ID);
        return sessionId != null && sessionId.equals(dataManager.getSessionId());
    }

    private String getSelectedGuidExtra(@NonNull Intent intent) {
        String selectedGuid = intent.getStringExtra(SIMPRINTS_SELECTED_GUID);
        return selectedGuid != null ? selectedGuid : "null";
    }

    private void onHandleNonNullIntent(@NonNull Intent intent) {
        boolean isValidSelection = hasValidApiKeyExtra(intent) && hasValidSessionIdExtra(intent);
        String selectedGuid = getSelectedGuidExtra(intent);
        if (isValidSelection) {
            dataManager.updateIdentification(selectedGuid);
        }
        dataManager.logGuidSelectionService(selectedGuid, isValidSelection);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            onHandleNonNullIntent(intent);
        }
    }
}
