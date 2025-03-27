package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import androidx.annotation.Keep

@Keep
internal data class ApiFingerprintTemplate(
    val template: String,
    val finger: ApiFinger,
)
