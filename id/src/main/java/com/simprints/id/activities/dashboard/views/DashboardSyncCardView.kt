package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.data.db.sync.models.SyncManagerState
import com.simprints.id.data.db.sync.room.SyncStatus
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.db.sync.viewModel.SyncStatusViewModel
import com.simprints.id.services.progress.DownloadProgress
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncCard: CardView = rootView.findViewById(R.id.dashboardCardSync)

    private val syncStateIcon: ImageView = rootView.findViewById(R.id.dashboardCardSyncState)
    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)

    private val syncProgressBar: ProgressBar = rootView.findViewById(R.id.dashboardCardSyncProgressBar)
    private val syncAction: TextView = rootView.findViewById(R.id.dashboardCardSyncAction)

    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    fun updateCard(cardModel: DashboardCard) {
        bind(cardModel)
    }

    override fun bind(cardModel: DashboardCard) {
        super.bind(cardModel)

        val component = (rootView.context.applicationContext as Application).component
        component.inject(this)

        if (cardModel is DashboardSyncCard) {
            cardModel.cardView = this
            setUploadCounter(cardModel)
            setDownloadCounter(cardModel)
            updateState(cardModel)
        }
    }

    fun updateState(cardModel: DashboardSyncCard) {
        when (cardModel.syncState) {
            SyncManagerState.NOT_STARTED -> setUIForSyncNotStarted(cardModel)
            SyncManagerState.STARTED -> setUIForSyncStarted()
            SyncManagerState.IN_PROGRESS -> setUIForSyncInProgress(cardModel)
            SyncManagerState.SUCCEED -> setUIForSyncSucceeded(cardModel)
            SyncManagerState.FAILED -> setUIForSyncFailed(cardModel)
        }
    }

    private fun setUIForSyncNotStarted(dataModel: DashboardSyncCard) {
        syncStateIcon.visibility = View.INVISIBLE

        syncProgressBar.visibility = View.INVISIBLE

        if (dataModel.syncNeeded) {
            showSyncNeededText()
        } else {
            showLastSyncTimeText(dataModel)
        }

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncStarted() {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_syncing)
        }

        syncProgressBar.visibility = View.INVISIBLE

        syncDescription.textResource = R.string.syncing_calculating

        disableSyncButton()
    }

    private fun setUIForSyncSucceeded(dataModel: DashboardSyncCard) {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_sync_success)
        }

        syncProgressBar.visibility = View.INVISIBLE

        showLastSyncTimeText(dataModel)

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncFailed(dataModel: DashboardSyncCard) {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_sync_failed)
        }

        syncProgressBar.visibility = View.INVISIBLE

        syncDescription.textResource = R.string.dashboard_card_sync_failed

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncInProgress(dataModel: DashboardSyncCard) {
        syncStateIcon.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_syncing)
        }

        val progressEmitted = dataModel.progress
        updateCountersDuringSync(progressEmitted)
        updateProgressBar(progressEmitted)

        description.text = ""

        syncDescription.textResource = R.string.syncing

        disableSyncButton()
    }

    private fun updateProgressBar(progressEmitted: Progress?) {
        syncProgressBar.apply {
            visibility = View.VISIBLE
            isIndeterminate = false
            max = progressEmitted?.maxValue ?: 0
            progress = progressEmitted?.currentValue ?: 0
        }
    }

    private fun updateCountersDuringSync(progress: Progress?) {
        when (progress) {
            null -> {
                syncDownloadCount.text = ""
                syncUploadCount.text = ""
            }
            is DownloadProgress -> syncDownloadCount.text = "${Math.max(progress.maxValue - progress.currentValue, 0)}"
            is UploadProgress -> syncUploadCount.text = "${Math.max(progress.maxValue - progress.currentValue, 0)}"
        }
    }

    private fun setUploadCounter(cardModel: DashboardSyncCard) {
        syncUploadCount.text = "${Math.max(cardModel.peopleToUpload, 0)}"
    }

    private fun setDownloadCounter(cardModel: DashboardSyncCard) {

        val observer = Observer<SyncStatus> {
            cardModel.peopleToDownload = it.peopleToDownSync
            syncDownloadCount.text = "${Math.max(it.peopleToDownSync, 0)}"
            if (it.peopleToDownSync > 0) {
                cardModel.syncNeeded = true
            }

        }
        val syncStatusViewModel = SyncStatusViewModel(syncStatusDatabase)
        syncStatusViewModel.syncStatus.observe(rootView.context as DashboardActivity, observer)
    }

    private fun showLastSyncTimeText(dataModel: DashboardSyncCard) {
        syncDescription.text = ""
        dataModel.lastSyncTime?.let {
            syncDescription.text = String.format(rootView.context.getString(R.string.dashboard_card_sync_last_sync), it)
        }
    }

    private fun showSyncNeededText() {
        syncDescription.textResource = R.string.dashboard_card_sync_needed
    }

    private fun enableSyncButton(dataModel: DashboardSyncCard) {
        syncAction.apply {
            textColor = ContextCompat.getColor(rootView.context, R.color.colorAccent)
        }
        syncCard.apply {
            setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
        }
    }

    private fun disableSyncButton() {
        syncAction.apply {
            textColor = ContextCompat.getColor(rootView.context, R.color.simprints_grey)
        }
        syncCard.apply {
            setOnClickListener { }
        }
    }
}
