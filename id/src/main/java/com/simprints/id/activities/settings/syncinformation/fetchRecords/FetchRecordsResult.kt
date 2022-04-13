package com.simprints.id.activities.settings.syncinformation.fetchRecords

import androidx.annotation.Keep
import com.simprints.id.activities.settings.syncinformation.SyncInformationViewModel

@Keep
data class FetchRecordsResult(
    val downSyncCount: SyncInformationViewModel.DownSyncCounts?,
    val isFailureBackendMaintenance: Boolean,
    val estimatedOutage: Long? = null
)
