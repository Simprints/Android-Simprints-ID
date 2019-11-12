package com.simprints.id.data.db.person.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.Person
import java.util.*

@Keep
data class ApiGetPerson(val id: String,
                        val projectId: String,
                        val userId: String,
                        val moduleId: String,
                        val createdAt: Date?,
                        val updatedAt: Date?,
                        val fingerprints: List<ApiFingerprintSample>? = null,
                        var faces: List<ApiFaceSample>? = null,
                        val deleted: Boolean)

fun Person.fromDomainToGetApi(): ApiGetPerson =
    ApiGetPerson(
        id = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprints = fingerprintSamples.map { it.fromDomainToApi() },
        deleted = false
    )

fun ApiGetPerson.fromGetApiToDomain(): Person =
    Person(
        patientId = id,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprintSamples = fingerprints?.map { it.fromApiToDomain() } ?: emptyList(),
        toSync = false
    )
