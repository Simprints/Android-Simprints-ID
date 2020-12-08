package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Subject(
    val subjectId: String,
    val projectId: String,
    val attendantId: String,
    val moduleId: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var fingerprintSamples: List<FingerprintSample> = emptyList(),
    var faceSamples: List<FaceSample> = emptyList(),

    @Deprecated("See SubjectToEventDbMigrationManagerImpl doc")
    val toSync: Boolean = false) : Parcelable
