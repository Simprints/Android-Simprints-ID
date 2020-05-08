package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import com.simprints.id.data.db.subject.domain.FaceSample.Companion.extractFaceSamplesFromBiometricReferences
import com.simprints.id.data.db.subject.domain.FingerprintSample.Companion.extractFingerprintSamplesFromBiometricReferences
import com.simprints.id.data.db.subject.domain.subjectevents.EnrolmentRecordCreationPayload
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
    var toSync: Boolean = true,
    var fingerprintSamples: List<FingerprintSample> = emptyList(),
    var faceSamples: List<FaceSample> = emptyList()) : Parcelable {

    companion object {
        fun buildSubjectFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
            Subject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                toSync = false,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }
    }
}
