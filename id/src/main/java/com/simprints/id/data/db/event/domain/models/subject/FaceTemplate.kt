package com.simprints.id.data.db.event.domain.models.subject

import com.simprints.id.data.db.event.remote.events.subject.ApiFaceTemplate

data class FaceTemplate(val template: String)

fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)
