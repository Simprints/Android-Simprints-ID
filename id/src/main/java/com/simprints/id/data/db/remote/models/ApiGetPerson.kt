package com.simprints.id.data.db.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.Person
import com.simprints.id.domain.fingerprint.Fingerprint
import java.util.*
import kotlin.collections.ArrayList

@Keep
data class ApiGetPerson(val id: String,
                        val projectId: String,
                        val userId: String,
                        val moduleId: String,
                        val createdAt: Date?,
                        val updatedAt: Date?,
                        val fingerprints: List<ApiFingerprint>? = null,
                        var faces: List<ApiFace>? = null)

fun Person.toApiGetPerson(): ApiGetPerson =
    ApiGetPerson(
        id = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprints = fingerprints.map { it.toApiFingerprint() }
    )

fun ApiGetPerson.toDomainPerson(): Person =
    Person(
        patientId = id,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprints = fingerprints?.map { it.toDomainFingerprint() } ?: emptyList(),
        toSync = false
    )
