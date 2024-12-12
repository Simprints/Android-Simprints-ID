package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.UpSyncEnrolmentRecords

@Keep
internal data class ApiUpSyncEnrolmentRecords(
    val id: String,
    val subjectIds: List<String> = listOf(),
) {
    fun fromApiToDomain(): UpSyncEnrolmentRecords = UpSyncEnrolmentRecords(id, subjectIds)
}
