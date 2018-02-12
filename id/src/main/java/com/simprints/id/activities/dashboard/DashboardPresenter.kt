package com.simprints.id.activities.dashboard

class DashboardPresenter(val view: DashboardContract.View) : DashboardContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun start() {
    }
}
