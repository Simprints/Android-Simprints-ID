package com.simprints.id.activities.dashboard

import androidx.lifecycle.LifecycleOwner
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.dashboard.viewModels.CardViewModel
import com.simprints.id.domain.ALERT_TYPE

interface DashboardContract {

    interface View : BaseView<Presenter> {
        fun updateCardViews()
        fun stopRequestIfRequired()
        fun launchAlertView(error: ALERT_TYPE)
        fun notifyCardViewChanged(position: Int)
        fun getStringWithParams(stringRes: Int, currentValue: Int = 0, maxValue: Int = 0): String
        fun showConfirmationDialogForLogout()
        fun showToastForUserOffline()
        fun showToastForRecordsUpToDate()
    }

    interface Presenter : BasePresenter {
        val cardsViewModelsList: ArrayList<CardViewModel>

        fun userDidWantToRefreshCardsIfPossible()
        fun userDidWantToDownSync()
        fun userDidWantToLogout()
        fun logout()
    }
}
