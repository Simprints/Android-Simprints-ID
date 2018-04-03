package com.simprints.id.activities.dashboard.views

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardLocalDbCard
import com.simprints.id.activities.dashboard.models.SyncUIState

class DashboardLocalDbCardView(itemView: View) : DashboardCardView(itemView) {
    private val syncStateIcon: ImageView = itemView.findViewById(R.id.dashboardCardSyncState)
    private val syncProgress: ProgressBar = itemView.findViewById(R.id.dashboardCardSyncProgress)
    private val syncAction: TextView = itemView.findViewById(R.id.dashboardCardSyncAction)

    override fun bind(dataModel: DashboardCard) {
        super.bind(dataModel)
        if (dataModel is DashboardLocalDbCard) {
            syncAction.setOnClickListener { }

            when (dataModel.syncState) {
                SyncUIState.IN_PROGRESS -> setUIForSyncInProgress()
                SyncUIState.FAILED -> setUIForSyncFailed(dataModel)
                SyncUIState.SUCCEED -> setUIForSyncSucceed(dataModel)
                SyncUIState.NOT_STARTED -> setUIForSyncNotStarted(dataModel)
            }
        }
    }

    private fun setUIForSyncNotStarted(dataModel: DashboardLocalDbCard) {
        syncStateIcon.visibility = View.INVISIBLE
        syncProgress.visibility = View.INVISIBLE
        syncAction.setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
    }

    private fun setUIForSyncSucceed(dataModel: DashboardLocalDbCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_sync_success)

        syncProgress.visibility = View.VISIBLE
        syncAction.setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
    }

    private fun setUIForSyncFailed(dataModel: DashboardLocalDbCard) {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_sync_failed)

        syncProgress.visibility = View.VISIBLE
        syncAction.setOnClickListener { dataModel.onSyncActionClicked(dataModel) }
    }

    private fun setUIForSyncInProgress() {
        syncStateIcon.visibility = View.VISIBLE
        syncStateIcon.setImageResource(R.drawable.ic_syncing)

        syncProgress.visibility = View.VISIBLE
        syncAction.setOnClickListener { }
    }
}
