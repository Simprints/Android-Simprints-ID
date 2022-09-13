package com.simprints.id.secure.models.remote

import androidx.annotation.Keep
import com.simprints.id.secure.models.UpSyncEnrolmentRecords

@Keep
data class ApiUpSyncEnrolmentRecords(val id: String, val subjectIds: List<String> = listOf())

fun ApiUpSyncEnrolmentRecords.fromApiToDomain(): UpSyncEnrolmentRecords =
    UpSyncEnrolmentRecords(id, subjectIds)
