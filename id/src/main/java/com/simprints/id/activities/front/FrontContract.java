package com.simprints.id.activities.front;


import android.content.Context;

import com.simprints.id.activities.BasePresenter;
import com.simprints.id.activities.BaseView;

interface FrontContract {

    interface View extends BaseView<Presenter> {

        void setSyncUnavailable();

        void setSyncInProgress();

        void setSyncSuccess();

        void setSyncFailed();
    }

    interface Presenter extends BasePresenter {

        void sync(Context appContext);

        void stopListening();
    }
}
