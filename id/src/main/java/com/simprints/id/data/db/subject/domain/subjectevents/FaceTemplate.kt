package com.simprints.id.data.db.subject.domain.personevents

import com.simprints.id.data.db.subject.remote.models.personevents.ApiFaceTemplate

data class FaceTemplate(val template: String)

fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)
