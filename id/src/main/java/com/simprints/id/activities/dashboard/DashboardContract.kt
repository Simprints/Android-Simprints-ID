package com.simprints.id.activities.dashboard

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.Constants
import com.simprints.id.domain.ALERT_TYPE

interface DashboardContract {

    interface View : BaseView<Presenter> {
        fun getStringWithParams(stringRes: Int, currentValue: Int = 0, maxValue: Int = 0): String
        fun setSyncItem(enabled: Boolean, string: String, icon: Int)
        fun showToast(messageRes: Int)
        fun launchAlertView(error: ALERT_TYPE)
    }

    interface Presenter : BasePresenter {
        fun pause()
        fun didUserWantToSyncBy(user: Constants.GROUP)
    }
}
