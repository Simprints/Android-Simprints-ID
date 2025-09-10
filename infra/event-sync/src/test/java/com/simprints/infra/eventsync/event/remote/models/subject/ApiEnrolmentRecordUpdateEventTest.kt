package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import org.junit.Test

class ApiEnrolmentRecordUpdateEventTest {
    @Test
    fun convert_EnrolmentRecordUpdateEvent() {
        val apiPayload = ApiEnrolmentRecordUpdatePayload(
            subjectId = "subjectId",
            biometricReferencesAdded = listOf(
                ApiFingerprintReference(
                    "fpRefId",
                    listOf(
                        ApiFingerprintTemplate("template", IFingerIdentifier.LEFT_THUMB),
                    ),
                    "NEC_1",
                ),
                ApiFaceReference(
                    "fRefId",
                    listOf(ApiFaceTemplate("template")),
                    "ROC_3",
                ),
            ),
            biometricReferencesRemoved = listOf("fpRefId2"),
            externalCredentialAdded = ApiExternalCredential(
                id = "id",
                type = ExternalCredentialType.NHISCard.toString(),
                value = "value"
            )
        )
        val expectedPayload = EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload(
            subjectId = "subjectId",
            biometricReferencesAdded = listOf(
                FingerprintReference(
                    "fpRefId",
                    listOf(FingerprintTemplate("template", IFingerIdentifier.LEFT_THUMB)),
                    "NEC_1",
                ),
                FaceReference(
                    "fRefId",
                    listOf(FaceTemplate("template")),
                    "ROC_3",
                ),
            ),
            biometricReferencesRemoved = listOf("fpRefId2"),
            externalCredentialAdded = ExternalCredential(
                id = "id",
                value = "value".asTokenizableEncrypted(),
                subjectId = "subjectId",
                type = ExternalCredentialType.NHISCard
            )
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
