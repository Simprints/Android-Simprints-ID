package com.simprints.id.activities.dashboard.models

import com.simprints.id.activities.dashboard.views.DashboardSyncCardView
import com.simprints.id.services.progress.Progress

class DashboardSyncCard(override val type: DashboardCardType,
                        override val position: Int,
                        override val imageRes: Int,
                        override val title: String,
                        val peopleToUpload: Int,
                        val peopleToDownload: Int,
                        var lastSyncTime: String?,
                        val syncNeeded: Boolean,
                        val onSyncActionClicked: (cardModel: DashboardSyncCard) -> Unit) : DashboardCard(type, position, imageRes, title, "") {

    var progress: Progress? = null

    var cardView: DashboardSyncCardView? = null

    var syncState: SyncUIState = SyncUIState.NOT_STARTED
        set(value) {
            field = value
            if(field != SyncUIState.IN_PROGRESS) {
                this.progress = null
            }

            cardView?.updateState(this)
        }


    fun onSyncProgress(progress: Progress) {
        this.progress = progress
        syncState = SyncUIState.IN_PROGRESS
    }

    fun onSyncComplete() {
        syncState = SyncUIState.SUCCEED
    }

    fun onSyncError(throwable: Throwable) {
        syncState = SyncUIState.FAILED
    }

    fun syncStarted() {
        syncState = SyncUIState.STARTED
    }
}

