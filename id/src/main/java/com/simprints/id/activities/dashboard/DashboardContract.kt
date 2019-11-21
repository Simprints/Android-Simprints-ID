package com.simprints.id.activities.dashboard

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.dashboard.viewModels.CardViewModel

interface DashboardContract {

    interface View : BaseView<Presenter> {
        fun updateCardViews()
        fun stopRequestIfRequired()
        fun notifyCardViewChanged(position: Int)
        fun showToastForUserOffline()
        fun showToastForRecordsUpToDate()
        fun showToastForNoModulesSelected()
        fun startCheckLoginActivityAndFinish()
    }

    interface Presenter : BasePresenter {
        val cardsViewModelsList: ArrayList<CardViewModel>

        fun userDidWantToRefreshCardsIfPossible()
        fun userDidWantToDownSync()
        fun logout()
    }
}
