package com.simprints.id.activities.dashboard

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Constants

interface DashboardContract {

    interface View : BaseView<Presenter> {
        fun getStringWithParams(stringRes: Int, currentValue: Int = 0, maxValue: Int = 0): String
        fun showToast(messageRes: Int)
        fun launchAlertView(error: ALERT_TYPE)
        fun updateCardViews()
        fun notifyCardViewChanged(position: Int)
    }

    interface Presenter : BasePresenter {
        fun pause()
        fun didUserWantToSyncBy(user: Constants.GROUP)
        val cardsModelsList: ArrayList<DashboardCard>
    }
}
