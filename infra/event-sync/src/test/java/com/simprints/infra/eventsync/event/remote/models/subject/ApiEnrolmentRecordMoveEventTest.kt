package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import org.junit.Test

class ApiEnrolmentRecordMoveEventTest {
    @Test
    fun convert_EnrolmentRecordMoveEvent() {
        val apiPayload = ApiEnrolmentRecordMovePayload(
            ApiEnrolmentRecordMovePayload.ApiEnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                "moduleId1",
                "attendantId",
                listOf(
                    ApiFingerprintReference(
                        "fpRefId",
                        listOf(
                            ApiFingerprintTemplate("template", SampleIdentifier.LEFT_THUMB),
                        ),
                        "NEC_1",
                    ),
                ),
                ApiExternalCredential(
                    id = "id",
                    value = "value",
                    type = ApiExternalCredentialType.NHIS_CARD,
                ),
            ),
            ApiEnrolmentRecordMovePayload.ApiEnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId2",
                "attendantId",
            ),
        )
        val expectedPayload = EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                "moduleId1".asTokenizableEncrypted(),
                "attendantId".asTokenizableEncrypted(),
                listOf(
                    FingerprintReference(
                        "fpRefId",
                        listOf(
                            FingerprintTemplate("template", SampleIdentifier.LEFT_THUMB),
                        ),
                        "NEC_1",
                    ),
                ),
                ExternalCredential(
                    id = "id",
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId2".asTokenizableEncrypted(),
                "attendantId".asTokenizableEncrypted(),
            ),
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
