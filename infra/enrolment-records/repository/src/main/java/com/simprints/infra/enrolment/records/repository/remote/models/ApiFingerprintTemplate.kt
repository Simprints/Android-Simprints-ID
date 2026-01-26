package com.simprints.infra.enrolment.records.repository.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFingerprintTemplate(
    val template: String,
    val finger: ApiFinger,
)
