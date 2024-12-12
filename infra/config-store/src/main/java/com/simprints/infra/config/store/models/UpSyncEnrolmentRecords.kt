package com.simprints.infra.config.store.models

data class UpSyncEnrolmentRecords(
    val id: String,
    val subjectIds: List<String>,
)
