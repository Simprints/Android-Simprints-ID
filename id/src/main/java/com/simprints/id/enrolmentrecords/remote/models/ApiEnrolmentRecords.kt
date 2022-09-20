package com.simprints.id.enrolmentrecords.remote.models

import androidx.annotation.Keep

@Keep
data class ApiEnrolmentRecords(val records: List<ApiEnrolmentRecord>)
