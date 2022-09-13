package com.simprints.id.secure.models.remote

import androidx.annotation.Keep
import com.simprints.id.secure.models.SyncEnrolmentRecord

@Keep
data class ApiSyncEnrolmentRecord(val id: String, val subjectIds: List<String>)

fun ApiSyncEnrolmentRecord.fromApiToDomain(): SyncEnrolmentRecord =
    SyncEnrolmentRecord(id, subjectIds)
