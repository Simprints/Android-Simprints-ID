package com.simprints.id.data.db.remote.models

import androidx.annotation.Keep
import com.simprints.id.domain.Person

@Keep
data class ApiPostPerson(val id: String,
                         val userId: String,
                         val moduleId: String,
                         val fingerprints: List<ApiFingerprint>? = null,
                         var faces: List<ApiFace>? = null)

fun Person.toApiPostPerson(): ApiPostPerson =
    ApiPostPerson(
        id = patientId,
        userId = userId,
        moduleId = moduleId,
        fingerprints = fingerprints.map { it.toApiFingerprint() }
    )
