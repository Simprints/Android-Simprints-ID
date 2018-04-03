package com.simprints.id.activities.dashboard.models

class DashboardLocalDbCard(override val imageRes: Int,
                           override val title: String,
                           override val description: String,
                           val onSyncActionClicked: (cardModel: DashboardLocalDbCard) -> Unit) : DashboardCard(imageRes, title, description) {

    var syncState: SyncUIState = SyncUIState.NOT_STARTED
}
