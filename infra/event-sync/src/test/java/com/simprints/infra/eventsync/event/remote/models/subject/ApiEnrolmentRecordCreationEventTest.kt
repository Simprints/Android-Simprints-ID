package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import org.junit.Test

class ApiEnrolmentRecordCreationEventTest {
    @Test
    fun convert_EnrolmentRecordCreationEvent() {
        val apiPayload = ApiEnrolmentRecordCreationPayload(
            subjectId = "subjectId",
            projectId = "projectId",
            moduleId = "moduleId",
            attendantId = "attendantId",
            biometricReferences = listOf(
                ApiFingerprintReference(
                    "fpRefId",
                    listOf(
                        ApiFingerprintTemplate("template", IFingerIdentifier.LEFT_THUMB),
                    ),
                    "NEC_1",
                ),
            ),
            externalCredential = ApiExternalCredential(
                id = "id",
                type = ExternalCredentialType.NHISCard.toString(),
                value = "value"
            ),
        )
        val expectedPayload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            subjectId = "subjectId",
            projectId = "projectId",
            moduleId = "moduleId".asTokenizableEncrypted(),
            attendantId = "attendantId".asTokenizableEncrypted(),
            biometricReferences = listOf(
                FingerprintReference(
                    "fpRefId",
                    listOf(
                        FingerprintTemplate("template", IFingerIdentifier.LEFT_THUMB),
                    ),
                    "NEC_1",
                ),
            ),
            externalCredentials = listOf(
                ExternalCredential(
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard
                )
            ),
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
