package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.id.data.db.person.domain.FaceSample.Companion.extractFaceSamplesFromBiometricReferences
import com.simprints.id.data.db.person.domain.FingerprintSample.Companion.extractFingerprintSamplesFromBiometricReferences
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordCreationPayload
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Person(
    val patientId: String,
    val projectId: String,
    val userId: String,
    val moduleId: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var toSync: Boolean = true,
    var fingerprintSamples: List<FingerprintSample> = emptyList(),
    var faceSamples: List<FaceSample> = emptyList()) : Parcelable {

    companion object {
        fun buildPersonFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
            Person(
                patientId = subjectId,
                projectId = projectId,
                userId = attendantId,
                moduleId = moduleId,
                toSync = false,
                fingerprintSamples = extractFingerprintSamplesFromBiometricReferences(this.biometricReferences),
                faceSamples = extractFaceSamplesFromBiometricReferences(this.biometricReferences)
            )
        }
    }
}
