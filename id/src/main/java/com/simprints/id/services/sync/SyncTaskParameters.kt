package com.simprints.id.services.sync

import com.simprints.id.services.progress.service.ProgressTaskParameters

sealed class SyncTaskParameters : ProgressTaskParameters {

    data class UserSyncTaskParameters(val projectId: String, val userId: String) : SyncTaskParameters()

    data class GlobalSyncTaskParameters(val projectId: String) : SyncTaskParameters()
}
