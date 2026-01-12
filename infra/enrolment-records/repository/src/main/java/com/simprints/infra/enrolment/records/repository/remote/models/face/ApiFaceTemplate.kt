package com.simprints.infra.enrolment.records.repository.remote.models.face

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFaceTemplate(
    val template: String,
)
