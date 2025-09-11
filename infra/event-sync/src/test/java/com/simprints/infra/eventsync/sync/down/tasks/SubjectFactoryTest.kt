package com.simprints.infra.eventsync.sync.down.tasks

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

class SubjectFactoryTest {
    @MockK
    lateinit var encodingUtils: EncodingUtils

    @MockK
    lateinit var timeHelper: TimeHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(UUID::class)

        every { encodingUtils.base64ToBytes(any()) } returns BASE_64_BYTES
        factory = SubjectFactory(
            encodingUtils = encodingUtils,
            timeHelper = timeHelper,
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(UUID::class)
    }

    @Test
    fun `when buildSubjectFromCreationPayload is called, correct samples are built`() {
        val payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            biometricReferences = listOf(FINGERPRINT_REFERENCE, faceReference),
            externalCredentials = emptyList()
        )
        val result = factory.buildSubjectFromCreationPayload(payload)
        val expected = Subject(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            fingerprintSamples = listOf(
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
            faceSamples = listOf(
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `when buildSubjectFromMovePayload is called, correct samples are built`() {
        val payload = EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            biometricReferences = listOf(FINGERPRINT_REFERENCE, faceReference),
            externalCredential = null
        )
        val result = factory.buildSubjectFromMovePayload(payload)

        val expected = Subject(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            fingerprintSamples = listOf(
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
            faceSamples = listOf(
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `when buildSubjectFromUpdatePayload is called, correct samples list is created`() {
        val subject = Subject(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            fingerprintSamples = listOf(
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-1",
                ),
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-2",
                ),
            ),
            faceSamples = listOf(
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-3",
                ),
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-4",
                ),
            ),
        )

        val payload = EnrolmentRecordUpdatePayload(
            subjectId = SUBJECT_ID,
            biometricReferencesRemoved = listOf("referenceId-finger-3", "referenceId-finger-2"),
            biometricReferencesAdded = listOf(
                FingerprintReference(
                    id = "referenceId-finger-5",
                    format = REFERENCE_FORMAT,
                    templates = listOf(
                        FingerprintTemplate(
                            template = BASE_64_BYTES.toString(),
                            finger = IFingerIdentifier.LEFT_THUMB,
                        ),
                    ),
                ),
                FaceReference(
                    id = "referenceId-finger-6",
                    format = REFERENCE_FORMAT,
                    templates = listOf(FaceTemplate(template = BASE_64_BYTES.toString())),
                ),
            ),
            externalCredentialAdded = EXTERNAL_CREDENTIAL
        )
        val result = factory.buildSubjectFromUpdatePayload(subject, payload)

        val expected = Subject(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            fingerprintSamples = listOf(
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-1",
                ),
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-5",
                ),
            ),
            faceSamples = listOf(
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-4",
                ),
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = "referenceId-finger-6",
                ),
            ),
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `when buildSubjectFromCaptureResults is called, correct subject is built`() {
        every { UUID.randomUUID().toString() } returns SUBJECT_ID

        val expected = Subject(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            createdAt = Date(0L),
            fingerprintSamples = listOf(
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
            faceSamples = listOf(
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
            externalCredentials = listOf(EXTERNAL_CREDENTIAL)
        )

        val result = factory.buildSubjectFromCaptureResults(
            subjectId = expected.subjectId,
            projectId = expected.projectId,
            attendantId = expected.attendantId,
            moduleId = expected.moduleId,
            fingerprintResponse = FingerprintCaptureResult(
                GUID1,
                listOf(
                    FingerprintCaptureResult.Item(
                        captureEventId = GUID1,
                        identifier = IDENTIFIER,
                        sample = FingerprintCaptureResult.Sample(
                            template = BASE_64_BYTES,
                            templateQualityScore = QUALITY,
                            format = REFERENCE_FORMAT,
                            imageRef = null,
                            fingerIdentifier = IDENTIFIER,
                        ),
                    ),
                ),
            ),
            faceResponse = FaceCaptureResult(
                GUID1,
                listOf(
                    FaceCaptureResult.Item(
                        captureEventId = GUID1,
                        index = 0,
                        sample = FaceCaptureResult.Sample(
                            template = BASE_64_BYTES,
                            format = REFERENCE_FORMAT,
                            faceId = REFERENCE_ID,
                            imageRef = null,
                        ),
                    ),
                ),
            ),
            externalCredential = EXTERNAL_CREDENTIAL
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `when buildSubject is called, correct subject is built`() {
        val expected = Subject(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            fingerprintSamples = listOf(
                FingerprintSample(
                    fingerIdentifier = IDENTIFIER,
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
            faceSamples = listOf(
                FaceSample(
                    template = BASE_64_BYTES,
                    format = REFERENCE_FORMAT,
                    referenceId = REFERENCE_ID,
                ),
            ),
            externalCredentials = listOf(EXTERNAL_CREDENTIAL)
        )

        val result = factory.buildSubject(
            subjectId = expected.subjectId,
            projectId = expected.projectId,
            attendantId = expected.attendantId,
            moduleId = expected.moduleId,
            fingerprintSamples = expected.fingerprintSamples,
            faceSamples = expected.faceSamples,
            externalCredentials = expected.externalCredentials
        )
        assertThat(result).isEqualTo(expected)
    }

    companion object {
        private lateinit var factory: SubjectFactory
        private const val PROJECT_ID = "projectId"
        private const val SUBJECT_ID = "subjectId"
        private const val EXTERNAL_CREDENTIAL_ID = "credentialId"
        private val ATTENDANT_ID = "encryptedAttendantId".asTokenizableRaw()
        private val MODULE_ID = "encryptedModuleId".asTokenizableRaw()
        private val BASE_64_BYTES = byteArrayOf(1)
        private const val REFERENCE_ID = "fpRefId"
        private const val REFERENCE_FORMAT = "NEC_1"
        private const val TEMPLATE_NAME = "template"
        private val EXTERNAL_CREDENTIAL_VALUE = "value".asTokenizableEncrypted()
        private val EXTERNAL_CREDENTIAL_TYPE = ExternalCredentialType.NHISCard
        private val IDENTIFIER = IFingerIdentifier.LEFT_THUMB
        private const val QUALITY = 10
        private val FINGERPRINT_REFERENCE = FingerprintReference(
            id = REFERENCE_ID,
            format = REFERENCE_FORMAT,
            templates = listOf(
                FingerprintTemplate(
                    template = TEMPLATE_NAME,
                    finger = IDENTIFIER,
                ),
            ),
        )
        private val faceReference = FaceReference(
            id = REFERENCE_ID,
            templates = listOf(FaceTemplate(TEMPLATE_NAME)),
            format = REFERENCE_FORMAT,
        )
        private val EXTERNAL_CREDENTIAL = ExternalCredential(
            id = EXTERNAL_CREDENTIAL_ID,
            value = EXTERNAL_CREDENTIAL_VALUE,
            subjectId = SUBJECT_ID,
            type = EXTERNAL_CREDENTIAL_TYPE
        )
    }
}
