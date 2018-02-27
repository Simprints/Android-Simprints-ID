package com.simprints.id.services.sync

import com.simprints.id.services.progress.service.ProgressTaskParameters

sealed class SyncTaskParameters : ProgressTaskParameters {

    data class UserSyncTaskParameters(val legacyApiKey: String, val userId: String) : SyncTaskParameters()

    data class GlobalSyncTaskParameters(val legacyApiKey: String) : SyncTaskParameters()
}
