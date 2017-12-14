package com.simprints.id.activities.front;


import android.support.annotation.NonNull;

class FrontPresenter implements FrontContract.Presenter {

    @NonNull
    private final FrontContract.View frontView;

    /**
     * @param view The FrontActivity
     */
    FrontPresenter(@NonNull FrontContract.View view) {
        frontView = view;
        frontView.setPresenter(this);
    }

    @Override
    public void start() {
    }
}
