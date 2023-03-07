package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.infra.events.remote.models.subject.biometricref.face.ApiFaceTemplate

@Keep
data class FaceTemplate(val template: String)

fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)
