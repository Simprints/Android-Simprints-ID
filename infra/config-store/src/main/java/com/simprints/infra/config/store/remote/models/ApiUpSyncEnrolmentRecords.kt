package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.UpSyncEnrolmentRecords
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiUpSyncEnrolmentRecords(
    val id: String,
    val subjectIds: List<String> = listOf(),
) {
    fun fromApiToDomain(): UpSyncEnrolmentRecords = UpSyncEnrolmentRecords(id, subjectIds)
}
