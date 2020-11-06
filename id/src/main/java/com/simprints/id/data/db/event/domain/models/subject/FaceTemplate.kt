package com.simprints.id.data.db.event.domain.models.subject

import com.simprints.id.data.db.event.remote.models.subject.ApiFaceTemplate

data class FaceTemplate(val template: String)

fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)
