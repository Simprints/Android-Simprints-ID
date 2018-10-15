package com.simprints.id.domain

import java.util.*

data class Person (
    val patientId: String,
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val createdAt: Date?,
    val updatedAt: Date?,
    val toSync: Boolean,
    val fingerprints: List<Fingerprint>
)
