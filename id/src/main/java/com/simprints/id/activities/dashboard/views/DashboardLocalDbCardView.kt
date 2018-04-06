package com.simprints.id.activities.dashboard.views

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardLocalDbCard
import com.simprints.id.activities.dashboard.models.SyncUIState
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource

class DashboardLocalDbCardView(private val rootView: View) : DashboardCardView(rootView) {

    private val syncStateIcon: ImageView = rootView.findViewById(R.id.dashboardCardSyncState)
    private val syncDescription: TextView = rootView.findViewById(R.id.dashboardCardSyncDescription)
    private val syncProgressBar: ProgressBar = rootView.findViewById(R.id.dashboardCardSyncProgressBar)
    private val syncAction: TextView = rootView.findViewById(R.id.dashboardCardSyncAction)

    override fun bind(cardModel: DashboardCard) {
        super.bind(cardModel)
        if (cardModel is DashboardLocalDbCard) {
            syncAction.setOnClickListener { }

            when (cardModel.syncState) {
                SyncUIState.IN_PROGRESS -> setUIForSyncInProgress(cardModel)
                SyncUIState.FAILED -> setUIForSyncFailed(cardModel)
                SyncUIState.SUCCEED -> setUIForSyncSucceed(cardModel)
                SyncUIState.NOT_STARTED -> setUIForSyncNotStarted(cardModel)
            }
        }
    }

    private fun setUIForSyncNotStarted(dataModel: DashboardLocalDbCard) {
        syncStateIcon.visibility = View.INVISIBLE
        syncProgressBar.visibility = View.INVISIBLE

        if (dataModel.syncNeeded) {
            showSyncNeededText()
        } else {
            showLastSyncTimeText(dataModel)
        }

        syncAction.isEnabled = true
        syncAction.setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
    }

    private fun setUIForSyncSucceed(dataModel: DashboardLocalDbCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_sync_success)
        syncProgressBar.visibility = View.INVISIBLE

        showLastSyncTimeText(dataModel)

        enableSyncButton(dataModel)
    }

    private fun setUIForSyncFailed(dataModel: DashboardLocalDbCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_sync_failed)
        syncProgressBar.visibility = View.INVISIBLE

        syncDescription.textResource = R.string.nav_sync_failed

        enableSyncButton(dataModel)
    }

    @SuppressLint("SetTextI18n")
    private fun setUIForSyncInProgress(dataModel: DashboardLocalDbCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_syncing)
        syncProgressBar.visibility = View.VISIBLE

        val progressData = dataModel.progress
        if (progressData != null) {
            syncProgressBar.isIndeterminate = false
            syncProgressBar.max = progressData.maxValue
            syncProgressBar.progress = progressData.currentValue
        } else {
            syncProgressBar.isIndeterminate = true
        }

        syncDescription.textResource = R.string.syncing

        disableSyncButton(dataModel)
    }

    private fun showLastSyncTimeText(dataModel: DashboardLocalDbCard) {
        syncDescription.text = ""
        dataModel.lastSyncTime?.let {
            syncDescription.text = String.format(rootView.context.getString(R.string.dashboard_card_localdb_last_sync), it)
        }
    }

    private fun showSyncNeededText() {
        syncDescription.textResource = R.string.dashboard_card_localdb_sync_needed
    }

    private fun enableSyncButton(dataModel: DashboardLocalDbCard) {
        syncAction.isEnabled = true
        syncAction.textColor = ContextCompat.getColor(rootView.context, R.color.colorAccent)
        syncAction.setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
    }

    private fun disableSyncButton(dataModel: DashboardLocalDbCard) {
        syncAction.textColor = ContextCompat.getColor(rootView.context, R.color.simprints_grey)
    }
}
