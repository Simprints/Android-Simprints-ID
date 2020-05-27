package com.simprints.id.orchestrator

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.tools.TimeHelper
import io.kotlintest.shouldThrow
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class EnrolmentHelperImplTest {

    private lateinit var enrolmentHelper: EnrolmentHelper

    @MockK lateinit var personRepository: PersonRepository
    @MockK lateinit var sessionRepository: SessionRepository
    @MockK lateinit var timeHelper: TimeHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        enrolmentHelper = EnrolmentHelperImpl(personRepository, sessionRepository, timeHelper)
    }

    @Test
    fun withFingerprintResponse_shouldBuildPerson() {
        val request = mockRequest()
        val fingerprintResponse = mockFingerprintResponse()

        val person = enrolmentHelper.buildPerson(request.projectId, request.userId, request.moduleId, fingerprintResponse, null, timeHelper)
        val expectedFingerprints = fingerprintResponse.captureResult

        with(person) {
            assertThat(projectId).isEqualTo(request.projectId)
            assertThat(moduleId).isEqualTo(request.moduleId)
            assertThat(userId).isEqualTo(request.userId)
            assertThat(fingerprintSamples.size).isEqualTo(expectedFingerprints.size)
            fingerprintSamples.forEachIndexed { index, actualSample ->
                val expectedSample = expectedFingerprints[index]
                assertThat(actualSample.fingerIdentifier.toString()).isEqualTo(expectedSample.identifier.toString())
                expectedSample.sample?.template?.let { expectedTemplate ->
                    assertThat(actualSample.template.contentEquals(expectedTemplate)).isEqualTo(true)
                }
                assertThat(actualSample.templateQualityScore).isEqualTo(expectedSample.sample?.templateQualityScore)
            }
            assertThat(faceSamples).isEqualTo(emptyList<FaceSample>())
        }
    }

    @Test
    fun withFaceResponse_shouldBuildPerson() {
        val request = mockRequest()
        val faceResponse = mockFaceResponse()

        val person = enrolmentHelper.buildPerson(request.projectId, request.userId, request.moduleId, null, faceResponse, timeHelper)
        val expectedFaceSamples = faceResponse.capturingResult.mapNotNull {
            it.result?.template?.let { template ->
                FaceSample(template)
            }
        }

        with(person) {
            assertThat(projectId).isEqualTo(request.projectId)
            assertThat(moduleId).isEqualTo(request.moduleId)
            assertThat(userId).isEqualTo(request.userId)
            assertThat(fingerprintSamples).isEqualTo(emptyList<FaceSample>())
            faceSamples.forEachIndexed { index, faceSample ->
                assertThat(faceSample.id).isEqualTo(expectedFaceSamples[index].id)
                assertThat(faceSample.template.contentEquals(expectedFaceSamples[index].template)).isTrue()
            }
        }
    }

    @Test
    fun withFingerprintAndFaceResponses_shouldBuildPerson() {
        val request = mockRequest()
        val fingerprintResponse = mockFingerprintResponse()
        val faceResponse = mockFaceResponse()

        val person = enrolmentHelper.buildPerson(request.projectId, request.userId, request.moduleId, fingerprintResponse, faceResponse, timeHelper)
        val expectedFingerprints = fingerprintResponse.captureResult
        val expectedFaceSamples = faceResponse.capturingResult.mapNotNull {
            it.result?.template?.let { template ->
                FaceSample(template)
            }
        }

        with(person) {
            assertThat(projectId).isEqualTo(request.projectId)
            assertThat(moduleId).isEqualTo(request.moduleId)
            assertThat(userId).isEqualTo(request.userId)
            assertThat(fingerprintSamples.size).isEqualTo(expectedFingerprints.size)
            fingerprintSamples.forEachIndexed { index, actualSample ->
                val expectedSample = expectedFingerprints[index]
                assertThat(actualSample.fingerIdentifier.toString()).isEqualTo(expectedSample.identifier.toString())
                expectedSample.sample?.template?.let { expectedTemplate ->
                    assertThat(actualSample.template.contentEquals(expectedTemplate)).isEqualTo(true)
                }
                assertThat(actualSample.templateQualityScore).isEqualTo(expectedSample.sample?.templateQualityScore)
            }
            faceSamples.forEachIndexed { index, faceSample ->
                assertThat(faceSample.id).isEqualTo(expectedFaceSamples[index].id)
                assertThat(faceSample.template.contentEquals(expectedFaceSamples[index].template)).isTrue()
            }
        }
    }

    @Test
    fun withNoResponses_shouldThrowException() {
        val request = mockRequest()

        shouldThrow<Throwable> {
            enrolmentHelper.buildPerson(request.projectId, request.userId, request.moduleId, null, null, timeHelper)
        }
    }

    private fun mockRequest() = AppRequest.AppRequestFlow.AppEnrolRequest(
        "projectId", "userId", "moduleId", "metadata"
    )

    private fun mockFingerprintResponse(): FingerprintCaptureResponse {
        val fingerId = FingerIdentifier.LEFT_THUMB
        val captureResult = listOf(
            FingerprintCaptureResult(
                fingerId,
                FingerprintCaptureSample(
                    fingerId,
                    templateQualityScore = 10,
                    template = "template".toByteArray()
                )
            )
        )
        return FingerprintCaptureResponse(captureResult = captureResult)
    }

    private fun mockFaceResponse(): FaceCaptureResponse {
        return FaceCaptureResponse(
            listOf(
                FaceCaptureResult(
                    index = 0,
                    result = FaceCaptureSample("faceId", "faceId".toByteArray(), null)
                )
            )
        )
    }

}
