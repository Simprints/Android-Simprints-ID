package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardSyncCardViewModel
import com.simprints.id.tools.utils.AndroidResourcesHelper
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(rootView: View) : DashboardCardView(rootView) {

    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)
    private val syncButton: Button = rootView.findViewById(R.id.dashboardSyncCardSyncButton)
    private val totalPeopleInLocal: TextView = rootView.findViewById(R.id.totalPeopleInLocal)

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    private var syncButtonEnabled = true

    init {
        (rootView.context.applicationContext as Application).component.inject(this)
    }

    override fun bind(cardModel: DashboardCard) {
        super.bind(cardModel)

        if (cardModel is DashboardSyncCardViewModel) {
            cardModel.cardView = this
            updateViews(cardModel)
        }
    }

    private fun updateViews(cardModel: DashboardSyncCardViewModel) {
        setTotalPeopleInDbCounter(cardModel.peopleInDb)
        setUploadCounter(cardModel.peopleToUpload)
        setListenerForSyncButton(cardModel)
        setSyncButtonState(cardModel.isSyncRunning)
        setDownloadCounter(cardModel.peopleToDownload)
        setLastSyncTime(cardModel.lastSyncTime)
    }

    private fun setSyncButtonState(isSyncRunning: Boolean) {
        if (isSyncRunning) {
            disableSyncButtonIfEnabled()
        } else {
            enableSyncButtonIfDisabledAndUpdateSyncInfo()
        }
    }


    private fun setTotalPeopleInDbCounter(peopleInDb: Int) {
        totalPeopleInLocal.text = "${Math.max(peopleInDb, 0)}"
    }

    private fun setUploadCounter(peopleToUpload: Int) {
        syncUploadCount.text = "${Math.max(peopleToUpload, 0)}"
    }

    private fun setListenerForSyncButton(cardModel: DashboardSyncCardViewModel) {
        syncButton.setOnClickListener { cardModel.onSyncActionClicked(cardModel) }
    }

    private fun setLastSyncTime(lastSyncTime: String) {
        syncDescription.text = lastSyncTime
    }

    private fun setDownloadCounter(peopleToDownload: Int) {
        syncDownloadCount.text = "${Math.max(peopleToDownload, 0)}"
    }

    private fun disableSyncButtonIfEnabled() {
        if (syncButtonEnabled) {
            syncButtonEnabled = false
            syncButton.background = androidResourcesHelper.getDrawable(R.drawable.button_rounded_corners_disabled)
            syncButton.text = androidResourcesHelper.getString(R.string.syncing)
        }
    }

    private fun enableSyncButtonIfDisabledAndUpdateSyncInfo() {
        if (!syncButtonEnabled) {
            syncButtonEnabled = true
            syncButton.background = androidResourcesHelper.getDrawable(R.drawable.button_rounded_corners)
            syncButton.text = androidResourcesHelper.getString(R.string.dashboard_card_sync_now)
        }
    }
}
