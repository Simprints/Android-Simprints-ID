package com.simprints.infra.enrolment.records.store.remote.models

import androidx.annotation.Keep

@Keep
internal data class ApiEnrolmentRecords(
    val records: List<ApiEnrolmentRecord>,
)
