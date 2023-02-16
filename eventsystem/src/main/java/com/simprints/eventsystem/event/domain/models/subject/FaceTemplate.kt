package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.eventsystem.event.remote.models.subject.biometricref.face.ApiFaceTemplate

@Keep
data class FaceTemplate(val template: String)

fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)
