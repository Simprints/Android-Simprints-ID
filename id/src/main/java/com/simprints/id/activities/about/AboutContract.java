package com.simprints.id.activities.about;


import com.simprints.id.activities.BasePresenter;
import com.simprints.id.activities.BaseView;

interface AboutContract {

    interface View extends BaseView<Presenter> {

        void setVersionData(String appVersion, String libsimprintsVersion, String scannerVersion);

        void setDbCountData(String userCount, String moduleCount, String globalCount);

        void setRecoverDbAvailable();

        void setRecoverDbUnavailable();

        void setStartRecovering();

        void setSuccessRecovering();

        void setRecoveringFailed(String errorMessage);
    }

    interface Presenter extends BasePresenter {

        void recoverDb();
    }
}
