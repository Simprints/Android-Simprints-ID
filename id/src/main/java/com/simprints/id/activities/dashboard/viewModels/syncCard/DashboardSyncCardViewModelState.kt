package com.simprints.id.activities.dashboard.viewModels.syncCard

data class DashboardSyncCardViewModelState(var onSyncActionClicked: () -> Unit = {},
                                           var peopleToUpload: Int? = null,
                                           var peopleToDownload: Int? = null,
                                           var peopleInDb: Int? = null,
                                           var isDownSyncRunning: Boolean = false,
                                           var showSyncButton: Boolean = false,
                                           var lastSyncTime: String = "")
