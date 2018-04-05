package com.simprints.id.activities.dashboard.models

import com.simprints.id.services.progress.Progress

class DashboardLocalDbCard(override val position: Int,
                           override val imageRes: Int,
                           override val title: String,
                           override val description: String,
                           var lastSyncTime: String?,
                           val syncNeeded: Boolean,
                           val onSyncActionClicked: (cardModel: DashboardLocalDbCard) -> Unit,
                           var progress: Progress? = null) : DashboardCard(position, imageRes, title, description) {

    var syncState: SyncUIState = SyncUIState.NOT_STARTED
}
