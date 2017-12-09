package com.simprints.id.activities.front;


import android.content.Context;
import android.support.annotation.NonNull;

import com.simprints.id.backgroundSync.SyncService;
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
                        throw new RuntimeException();
                }
            }
        };
    }

    @Override
    public void sync(Context appContext) {
        frontView.setSyncInProgress();
        if (!syncService.startAndListen(appContext, dataCallback)) {
            frontView.setSyncUnavailable();
        }
    }

    @Override
    public void stopListening() {
        syncService.stopListening(dataCallback);
    }
}
