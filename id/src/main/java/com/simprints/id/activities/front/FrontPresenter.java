package com.simprints.id.activities.front;


import android.content.Context;
import android.support.annotation.NonNull;

import com.simprints.id.backgroundSync.SyncService;
import com.simprints.id.exceptions.unsafe.InvalidSyncParametersError;
import com.simprints.id.exceptions.unsafe.UnexpectedDataError;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;

class FrontPresenter implements FrontContract.Presenter {

    @NonNull
    private final FrontContract.View frontView;

    @NonNull
    private final SyncService syncService;

    private DataCallback dataCallback;

    /**
     * @param view The FrontActivity
     */
    FrontPresenter(@NonNull FrontContract.View view, @NonNull SyncService syncService) {
        frontView = view;
        this.syncService = syncService;
        frontView.setPresenter(this);
    }

    @Override
    public void start() {

        dataCallback = new DataCallback() {
            @Override
            public void onSuccess() {
                frontView.setSyncSuccess();
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                switch (data_error) {
                    case SYNC_INTERRUPTED:
                        frontView.setSyncFailed();
                        break;
                    default:
                        throw UnexpectedDataError.forDataError(data_error, "FrontPresenter");
                }
            }
        };
    }

    @Override
    public void sync(Context appContext) {
        frontView.setSyncInProgress();
        try {
            syncService.startAndListen(appContext, dataCallback);
        } catch (InvalidSyncParametersError e) {
            frontView.setSyncUnavailable();
        }
    }

    @Override
    public void stopListening() {
        syncService.stopListening(dataCallback);
    }
}
