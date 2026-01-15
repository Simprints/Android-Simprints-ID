package com.simprints.infra.eventsync.event.remote.models.subject

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.FingerprintReference
import com.simprints.infra.events.event.domain.models.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.ApiExternalCredential
import com.simprints.infra.eventsync.event.remote.ApiFingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.ApiEnrolmentRecordCreationPayload
import com.simprints.infra.eventsync.event.remote.models.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.fromApiToDomain
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
                        ApiFingerprintTemplate("template", TemplateIdentifier.LEFT_THUMB),
                    ),
                    "NEC_1",
                ),
            ),
            externalCredentials = listOf(
                ApiExternalCredential(
                    id = "id",
                    type = ApiExternalCredentialType.NHIS_CARD,
                    value = "value",
                ),
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
                        FingerprintTemplate("template", TemplateIdentifier.LEFT_THUMB),
                    ),
                    "NEC_1",
                ),
            ),
            externalCredentials = listOf(
                ExternalCredential(
                    id = "id",
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
        )

        assertThat(apiPayload.fromApiToDomain()).isEqualTo(expectedPayload)
    }
}
