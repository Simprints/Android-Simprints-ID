package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.id.data.db.subject.domain.FaceSample.Companion.extractFaceSamplesFromBiometricReferences
import com.simprints.id.data.db.subject.domain.FingerprintSample.Companion.extractFingerprintSamplesFromBiometricReferences
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
    var faceSamples: List<FaceSample> = emptyList()) : Parcelable {

    companion object {
        fun buildSubjectFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
            Subject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }

        fun buildSubjectFromCreationPayload(payload: EnrolmentRecordCreationInMove) = with(payload) {
            Subject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }
    }
}
