package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.State
import androidx.work.WorkStatus
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.db.sync.room.SyncStatus
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.db.sync.viewModel.SyncStatusViewModel
import com.simprints.id.data.prefs.PreferencesManager
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)
    private val syncButton: Button = rootView.findViewById(R.id.dashboardSyncCardSyncButton)
    private val totalPeopleInLocal: TextView = rootView.findViewById(R.id.totalPeopleInLocal)

    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var preferencesManager: PreferencesManager
    private lateinit var syncStatusViewModel: SyncStatusViewModel

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun updateCard(cardModel: DashboardCard) {
        bind(cardModel)
    }

    override fun bind(cardModel: DashboardCard) {
        super.bind(cardModel)

        val component = (rootView.context.applicationContext as Application).component
        component.inject(this)
        syncStatusViewModel = SyncStatusViewModel(syncStatusDatabase, preferencesManager.projectId)

        if (cardModel is DashboardSyncCard) {
            cardModel.cardView = this
            observeAndUpdatePeopleToDownloadAndLastSyncTime(cardModel)
            observeDownSyncStatusAndUpdateButton(cardModel)
            setTotalPeopleInDbCounter(cardModel)
            setUploadCounter(cardModel)
            setListenerForSyncButton(cardModel)
        }
    }

    private fun observeAndUpdatePeopleToDownloadAndLastSyncTime(cardModel: DashboardSyncCard) {

        val observer = Observer<SyncStatus> {
            cardModel.peopleToDownload = it.peopleToDownSync
            syncDownloadCount.text = "${Math.max(it.peopleToDownSync, 0)}"
            if (it.peopleToDownSync > 0) {
                cardModel.syncNeeded = true
            }
            calculateLastSyncTimeAndUpdateText(it)
        }
        syncStatusViewModel.syncStatus.observe(rootView.context as DashboardActivity, observer)
    }

    private fun observeDownSyncStatusAndUpdateButton(cardModel: DashboardSyncCard) {

        val downSyncObserver = Observer<MutableList<WorkStatus>> {
            if(it.size > 0) {
                if (it.last().state == State.RUNNING) {
                    disableSyncButtonIfEnabled()
                } else {
                    enableSyncButtonIfDisabledAndUpdateSyncInfo(cardModel)
                }
            }
        }
        syncStatusViewModel.downSyncWorkStatus.observe(rootView.context as DashboardActivity, downSyncObserver)
    }

    private fun setTotalPeopleInDbCounter(cardModel: DashboardSyncCard) {
        totalPeopleInLocal.text = "${Math.max(cardModel.peopleInDb, 0)}"
    }

    private fun setUploadCounter(cardModel: DashboardSyncCard) {
        syncUploadCount.text = "${Math.max(cardModel.peopleToUpload, 0)}"
    }

    private fun calculateLastSyncTimeAndUpdateText(syncStatus: SyncStatus) {
        val lastSyncTime = calculateLatestSyncTimeIfPossible(syncStatus.lastDownSyncTime, syncStatus.lastUpSyncTime)
        syncDescription.text = String.format(rootView.context.getString(R.string.dashboard_card_sync_last_sync),
            lastSyncTime)
    }

    private fun calculateLatestSyncTimeIfPossible(lastDownSyncTime: Long?, lastUpSyncTime: Long?): String {
        val lastDownSyncDate = lastDownSyncTime?.let { Date(it) }
        val lastUpSyncDate =  lastUpSyncTime?.let { Date(it) }

        if (lastDownSyncDate != null && lastUpSyncDate != null) {
            return if (lastDownSyncDate.after(lastUpSyncDate)) {
                lastDownSyncDate.toString()
            } else {
                lastUpSyncDate.toString()
            }
        }

        lastDownSyncDate?.let { return dateFormat.format(it) }
        lastUpSyncDate?.let { return dateFormat.format(it) }

        return ""
    }

    private fun setListenerForSyncButton(cardModel: DashboardSyncCard) {
        syncButton.setOnClickListener { cardModel.onSyncActionClicked(cardModel) }
    }

    private fun disableSyncButtonIfEnabled() {
        if (syncButton.isClickable) {
            syncButton.isClickable = false
            syncButton.background = ContextCompat.getDrawable(rootView.context, R.drawable.button_rounded_corners_disabled)
            syncButton.text = rootView.context.resources.getString(R.string.syncing)
        }
    }

    private fun enableSyncButtonIfDisabledAndUpdateSyncInfo(cardModel: DashboardSyncCard) {
        if(!syncButton.isClickable) {
            syncButton.isClickable = true
            syncButton.background = ContextCompat.getDrawable(rootView.context, R.drawable.button_rounded_corners)
            syncButton.text = rootView.context.resources.getString(R.string.dashboard_card_sync_now)
            cardModel.updateSyncInfo()
        }
    }
}
