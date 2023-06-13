package com.simprints.infra.projectsecurity.securitystate.repo.remote

import androidx.annotation.Keep
import com.simprints.infra.projectsecurity.securitystate.models.UpSyncEnrolmentRecords

@Keep
internal data class ApiUpSyncEnrolmentRecords(val id: String, val subjectIds: List<String> = listOf()) {

    fun fromApiToDomain(): UpSyncEnrolmentRecords = UpSyncEnrolmentRecords(id, subjectIds)
}
