package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.common.syntax.mock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class PersonBuilderTest {

    private val personBuilder = AppResponseBuilderForEnrol.PersonBuilder
    private val timeHelper: TimeHelper = mock()

    @Test
    fun withFingerprintResponse_shouldBuildPerson() {
        val request = mockRequest()
        val fingerprintResponse = mockFingerprintResponse()

        val person = personBuilder.buildPerson(request, fingerprintResponse, null, timeHelper)
        val expectedFingerprints = fingerprintResponse.captureResult

        with(person) {
            assertThat(projectId, `is`(request.projectId))
            assertThat(moduleId, `is`(request.moduleId))
            assertThat(userId, `is`(request.userId))
            assertThat(fingerprintSamples.size, `is`(expectedFingerprints.size))
            fingerprintSamples.forEachIndexed { index, actualSample ->
                val expectedSample = expectedFingerprints[index]
                assertThat(actualSample.fingerIdentifier.toString(), `is`(expectedSample.identifier.toString()))
                expectedSample.sample?.template?.let { expectedTemplate ->
                    assertThat(actualSample.template.contentEquals(expectedTemplate), `is`(true))
                }
                assertThat(actualSample.templateQualityScore, `is`(expectedSample.sample?.templateQualityScore))
            }
            assertThat(faceSamples, `is`(emptyList()))
        }
    }

    @Test
    fun withFaceResponse_shouldBuildPerson() {
        val request = mockRequest()
        val faceResponse = mockFaceResponse()

        val person = personBuilder.buildPerson(request, null, faceResponse, timeHelper)
        val expectedFaceSamples = faceResponse.capturingResult.mapNotNull {
            it.result?.template?.let { template ->
                val imageRef = it.result?.imageRef
                FaceSample(template, imageRef)
            }
        }

        with(person) {
            assertThat(projectId, `is`(request.projectId))
            assertThat(moduleId, `is`(request.moduleId))
            assertThat(userId, `is`(request.userId))
            assertThat(fingerprintSamples, `is`(emptyList()))
            faceSamples.forEachIndexed { index, faceSample ->
                assertThat(faceSample.id, `is`(expectedFaceSamples[index].id))
                assertThat(faceSample.imageRef?.uri, `is`(expectedFaceSamples[index].imageRef?.uri))
                assertThat(faceSample.template.contentEquals(expectedFaceSamples[index].template), `is`(true))
            }
        }
    }

    @Test
    fun withFingerprintAndFaceResponses_shouldBuildPerson() {
        val request = mockRequest()
        val fingerprintResponse = mockFingerprintResponse()
        val faceResponse = mockFaceResponse()

        val person = personBuilder.buildPerson(request, fingerprintResponse, faceResponse, timeHelper)
        val expectedFingerprints = fingerprintResponse.captureResult
        val expectedFaceSamples = faceResponse.capturingResult.mapNotNull {
            it.result?.template?.let { template ->
                val imageRef = it.result?.imageRef
                FaceSample(template, imageRef)
            }
        }

        with(person) {
            assertThat(projectId, `is`(request.projectId))
            assertThat(moduleId, `is`(request.moduleId))
            assertThat(userId, `is`(request.userId))
            assertThat(fingerprintSamples.size, `is`(expectedFingerprints.size))
            fingerprintSamples.forEachIndexed { index, actualSample ->
                val expectedSample = expectedFingerprints[index]
                assertThat(actualSample.fingerIdentifier.toString(), `is`(expectedSample.identifier.toString()))
                expectedSample.sample?.template?.let { expectedTemplate ->
                    assertThat(actualSample.template.contentEquals(expectedTemplate), `is`(true))
                }
                assertThat(actualSample.templateQualityScore, `is`(expectedSample.sample?.templateQualityScore))
            }
            faceSamples.forEachIndexed { index, faceSample ->
                assertThat(faceSample.id, `is`(expectedFaceSamples[index].id))
                assertThat(faceSample.imageRef?.uri, `is`(expectedFaceSamples[index].imageRef?.uri))
                assertThat(faceSample.template.contentEquals(expectedFaceSamples[index].template), `is`(true))
            }
        }
    }

    @Test
    fun withNoResponses_shouldThrowException() {
        val request = mockRequest()

        assertThrows<Throwable> {
            personBuilder.buildPerson(request, null, null, timeHelper)
        }
    }

    private fun mockRequest() = AppEnrolRequest(
        "projectId", "userId", "moduleId", "metadata"
    )

    private fun mockFingerprintResponse(): FingerprintCaptureResponse {
        val fingerId = FingerIdentifier.LEFT_THUMB
        val captureResult = listOf(
            FingerprintCaptureResult(
                fingerId,
                FingerprintSample(
                    fingerId,
                    imageRef = null,
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
