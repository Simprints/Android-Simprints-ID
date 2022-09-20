package com.simprints.id.enrolmentrecords.remote.models.fingerprint

import androidx.annotation.Keep

@Keep
data class ApiFingerprintTemplate(
    val quality: Int,
    val template: String,
    val finger: ApiFinger,
)

