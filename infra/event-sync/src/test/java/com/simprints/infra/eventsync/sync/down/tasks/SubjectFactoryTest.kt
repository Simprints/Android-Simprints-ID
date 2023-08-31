package com.simprints.infra.eventsync.sync.down.tasks

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test


class SubjectFactoryTest {

    private lateinit var factory: SubjectFactory
    private val projectId = "projectId"
    private val subjectId = "subjectId"
    private val attendantId = "encryptedAttendantId"
    private val moduleId = "encryptedModuleId"
    private val base64Bytes = byteArrayOf(1)
    private val referenceId = "fpRefId"
    private val referenceFormat = "NEC_1"
    private val templateName = "template"
    private val identifier = IFingerIdentifier.LEFT_THUMB
    private val quality = 10
    private val fingerprintReference = FingerprintReference(
        id = referenceId,
        format = referenceFormat,
        templates = listOf(
            FingerprintTemplate(
                quality = quality,
                template = templateName,
                finger = identifier
            )
        )
    )
    private val faceReference = FaceReference(
        id = referenceId,
        templates = listOf(FaceTemplate(templateName)),
        format = referenceFormat
    )

    @MockK
    lateinit var encodingUtils: EncodingUtils

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { encodingUtils.base64ToBytes(any()) } returns base64Bytes
        factory = SubjectFactory(
            encodingUtils = encodingUtils
        )
    }

    @Test
    fun `when buildSubjectFromCreationPayload is called, correct samples are built`() {
        val payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            biometricReferences = listOf(fingerprintReference, faceReference)
        )
        val result = factory.buildSubjectFromCreationPayload(payload)

        assertThat(result.subjectId).isEqualTo(subjectId)
        assertThat(result.projectId).isEqualTo(projectId)
        assertThat(result.attendantId).isEqualTo(attendantId)
        assertThat(result.moduleId).isEqualTo(moduleId)
        with(result.fingerprintSamples.first()) {
            assertThat(fingerIdentifier).isEqualTo(identifier)
            assertThat(template).isEqualTo(base64Bytes)
            assertThat(templateQualityScore).isEqualTo(quality)
            assertThat(format).isEqualTo(referenceFormat)
        }
        with(result.faceSamples.first()) {
            assertThat(template).isEqualTo(base64Bytes)
            assertThat(format).isEqualTo(referenceFormat)
        }
    }

    @Test
    fun `when buildSubjectFromMovePayload is called, correct samples are built`() {
        val payload = EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
            subjectId = subjectId,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            biometricReferences = listOf(fingerprintReference, faceReference)
        )
        val result = factory.buildSubjectFromMovePayload(payload)

        assertThat(result.subjectId).isEqualTo(subjectId)
        assertThat(result.projectId).isEqualTo(projectId)
        assertThat(result.attendantId).isEqualTo(attendantId)
        assertThat(result.moduleId).isEqualTo(moduleId)
        with(result.fingerprintSamples.first()) {
            assertThat(fingerIdentifier).isEqualTo(identifier)
            assertThat(template).isEqualTo(base64Bytes)
            assertThat(templateQualityScore).isEqualTo(quality)
            assertThat(format).isEqualTo(referenceFormat)
        }
        with(result.faceSamples.first()) {
            assertThat(template).isEqualTo(base64Bytes)
            assertThat(format).isEqualTo(referenceFormat)
        }
    }

}