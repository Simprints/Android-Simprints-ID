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
    @Deprecated("" +
        "Required to migrate from 2020.3.2. It can removed when 2020.3.2 is not supported anymore." +
        " After 2020.3.2, subjects are not synced anymore. Only events are synced.")
    val toSync: Boolean = false) : Parcelable
