package com.simprints.eventsystem.event.domain.models.subject

import com.simprints.eventsystem.event.remote.models.subject.biometricref.face.ApiFaceTemplate

data class FaceTemplate(val template: String)

fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)
