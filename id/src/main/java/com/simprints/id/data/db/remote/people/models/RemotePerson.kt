package com.simprints.id.data.db.remote.people.models

import com.simprints.id.data.db.remote.models.fb_Person

data class RemotePerson(
    val id: String,
    val moduleId: String,
    val userId: String,
    val createdAt: Long?,
    val fingerprints: Map<String, List<RemoteFingerprint>>
)

fun fb_Person.toRemotePerson() =
    RemotePerson(
        id = patientId,
        moduleId = moduleId,
        userId = userId,
        createdAt = createdAt?.time,
        fingerprints = fingerprints
            .map {  (fingerId, fingerprints) ->
                fingerId.name to fingerprints.map { fingerprint -> fingerprint.toRemoteFingerprint() }
            }
            .toMap()
    )
