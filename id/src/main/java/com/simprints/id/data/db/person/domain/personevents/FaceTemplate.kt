package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiFaceTemplate

data class FaceTemplate(val template: String)

fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)
