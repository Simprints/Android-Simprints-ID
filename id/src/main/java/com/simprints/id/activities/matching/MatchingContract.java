package com.simprints.id.activities.matching;


import android.content.Intent;

import com.simprints.id.activities.BasePresenter;
import com.simprints.id.activities.BaseView;
import com.simprints.id.model.ALERT_TYPE;

interface MatchingContract {

    interface View extends BaseView<Presenter> {

        void setIdentificationProgress(int progress);

        void setVerificationProgress();

        void setIdentificationProgressLoadingStart();

        void setIdentificationProgressMatchingStart(int matchSize);

        void setIdentificationProgressReturningStart();

        void setIdentificationProgressFinished(int returnSize, int tier1Or2Matches, int tier3Matches, int tier4Matches, int matchingEndWaitTimeMillis);

        void launchAlert(ALERT_TYPE alertType);

        void makeToastMatchNotRunning(String text);

        void doSetResult(int resultCode, Intent resultData);

        void doFinish();
    }

    interface Presenter extends BasePresenter {

    }
}
