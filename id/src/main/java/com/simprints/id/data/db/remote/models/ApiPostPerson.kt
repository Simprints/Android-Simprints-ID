package com.simprints.id.data.db.remote.models

import androidx.annotation.Keep
import com.simprints.id.domain.Person

@Keep
data class ApiPostPerson(val id: String,
                         val projectId: String,
                         val userId: String,
                         val moduleId: String,
                         val fingerprints: List<ApiFingerprint>? = null,
                         var faces: List<ApiFace>? = null)

fun Person.toApiPostPerson(): ApiPostPerson =
    ApiPostPerson(
        id = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        fingerprints = fingerprints.map { it.toApiFingerprint() }
    )

fun ApiPostPerson.toDomainPerson(): Person =
    Person(
        patientId = id,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        fingerprints = fingerprints?.map { it.toDomainFingerprint() } ?: emptyList(),
        toSync = false
    )
