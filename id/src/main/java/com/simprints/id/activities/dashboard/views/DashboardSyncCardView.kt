package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardSyncCard
import com.simprints.id.activities.dashboard.models.SyncUIState
import com.simprints.id.services.progress.DownloadProgress
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource

@SuppressLint("SetTextI18n")
class DashboardSyncCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncStateIcon: ImageView = rootView.findViewById(R.id.dashboardCardSyncState)
    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncUploadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncUploadText)
    private val syncDownloadCount: TextView = rootView.findViewById(R.id.dashboardCardSyncDownloadText)

    private val syncProgressBar: ProgressBar = rootView.findViewById(R.id.dashboardCardSyncProgressBar)
    private val syncAction: TextView = rootView.findViewById(R.id.dashboardCardSyncAction)

    override fun bind(cardModel: DashboardCard) {
        super.bind(cardModel)

        if (cardModel is DashboardSyncCard) {
            cardModel.cardView = this
            syncUploadCount.text = "${cardModel.peopleToUpload}"
            syncDownloadCount.text = "${cardModel.peopleToDownload}"

            updateState(cardModel)
        }
    }

    fun updateState(cardModel: DashboardSyncCard) {
        when (cardModel.syncState) {
            SyncUIState.NOT_STARTED -> setUIForSyncNotStarted(cardModel)
            SyncUIState.STARTED -> setUIForSyncStarted(cardModel)
            SyncUIState.IN_PROGRESS -> setUIForSyncInProgress(cardModel)
            SyncUIState.SUCCEED -> setUIForSyncSucceed(cardModel)
            SyncUIState.FAILED -> setUIForSyncFailed(cardModel)
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

    fun setUIForSyncStarted(dataModel: DashboardSyncCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_syncing)
        syncProgressBar.visibility = View.INVISIBLE

        syncDescription.textResource = R.string.syncing_calculating

        disableSyncButton(dataModel)
    }

    private fun setUIForSyncSucceed(dataModel: DashboardSyncCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_sync_success)
        syncProgressBar.visibility = View.INVISIBLE

        showLastSyncTimeText(dataModel)

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncFailed(dataModel: DashboardSyncCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_sync_failed)
        syncProgressBar.visibility = View.INVISIBLE

        syncDescription.textResource = R.string.nav_sync_failed

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncInProgress(dataModel: DashboardSyncCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_syncing)
        syncProgressBar.visibility = View.VISIBLE
        description.text = ""

        val progress = dataModel.progress
        updateCounters(progress)

        syncProgressBar.isIndeterminate = false
        syncProgressBar.max = progress?.maxValue ?: 0
        syncProgressBar.progress = progress?.currentValue ?: 0

        syncDescription.textResource = R.string.syncing

        disableSyncButton(dataModel)
    }

    private fun updateCounters(progress: Progress?) {
        when (progress) {
            null -> {
                syncDownloadCount.text = ""
                syncUploadCount.text = ""
            }
            is DownloadProgress -> syncDownloadCount.text = "${progress.maxValue - progress.currentValue}"
            is UploadProgress -> syncUploadCount.text = "${progress.maxValue - progress.currentValue}"
        }
    }

    private fun showLastSyncTimeText(dataModel: DashboardSyncCard) {
        syncDescription.text = ""
        dataModel.lastSyncTime?.let {
            syncDescription.text = String.format(rootView.context.getString(R.string.dashboard_card_localdb_last_sync), it)
        }
    }

    private fun showSyncNeededText() {
        syncDescription.textResource = R.string.dashboard_card_localdb_sync_needed
    }

    private fun enableSyncButton(dataModel: DashboardSyncCard) {
        syncAction.isEnabled = true
        syncAction.textColor = ContextCompat.getColor(rootView.context, R.color.colorAccent)
        syncAction.setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
    }

    private fun disableSyncButton(dataModel: DashboardSyncCard) {
        syncAction.textColor = ContextCompat.getColor(rootView.context, R.color.simprints_grey)
    }
}
