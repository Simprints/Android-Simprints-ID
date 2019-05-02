package com.simprints.id.data.db.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.id.domain.Person
import java.util.*
import kotlin.collections.ArrayList

@Keep
data class ApiPerson(@SerializedName("id") var patientId: String,
                     var projectId: String,
                     var userId: String,
                     var moduleId: String,
                     var createdAt: Date?,
                     var updatedAt: Date?,
                     var fingerprints:  ArrayList<ApiFingerprint>,
                     var faces: ArrayList<ApiFace>? = null)

fun Person.toApiPerson(): ApiPerson =
    ApiPerson(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprints = ArrayList(fingerprints.map { it.toApiFingerprint() })
    )

fun ApiPerson.toDomainPerson(): Person =
    Person(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        fingerprints = fingerprints.map { it.toDomainFingerprint() },
        toSync = false
    )
