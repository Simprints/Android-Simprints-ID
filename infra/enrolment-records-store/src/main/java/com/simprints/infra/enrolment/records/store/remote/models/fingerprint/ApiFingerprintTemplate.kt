package com.simprints.infra.enrolment.records.store.remote.models.fingerprint

import androidx.annotation.Keep

@Keep
internal data class ApiFingerprintTemplate(
    val quality: Int,
    val template: String,
    val finger: ApiFinger,
)
