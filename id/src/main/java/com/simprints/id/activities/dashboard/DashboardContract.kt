package com.simprints.id.activities.dashboard

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Constants

interface DashboardContract {

    interface View : BaseView<Presenter> {
        fun showToast(messageRes: Int)
        fun updateCardViews()
        fun stopRequestIfRequired()
        fun launchAlertView(error: ALERT_TYPE)
        fun notifyCardViewChanged(position: Int)
        fun getStringWithParams(stringRes: Int, currentValue: Int = 0, maxValue: Int = 0): String
    }

    interface Presenter : BasePresenter {
        val cardsModelsList: ArrayList<DashboardCard>

        fun pause()
        fun didUserWantToRefreshCards()
        fun didUserWantToSyncBy(user: Constants.GROUP)
    }
}
