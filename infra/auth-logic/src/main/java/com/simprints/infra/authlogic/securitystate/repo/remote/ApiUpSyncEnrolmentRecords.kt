package com.simprints.infra.authlogic.securitystate.repo.remote

import androidx.annotation.Keep
import com.simprints.infra.authlogic.securitystate.models.UpSyncEnrolmentRecords

@Keep
internal data class ApiUpSyncEnrolmentRecords(val id: String, val subjectIds: List<String> = listOf()) {

    fun fromApiToDomain(): UpSyncEnrolmentRecords = UpSyncEnrolmentRecords(id, subjectIds)
}
