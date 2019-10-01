package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
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

        with(person) {
            assertThat(patientId, `is`(EXPECTED_GUID_FINGERPRINT))
            assertThat(projectId, `is`(request.projectId))
            assertThat(moduleId, `is`(request.moduleId))
            assertThat(userId, `is`(request.userId))
            assertThat(fingerprintSamples, `is`(emptyList())) // TODO: validate once implemented
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
        val expectedFaceSamples = faceResponse.capturingResult.mapNotNull {
            it.result?.template?.let { template ->
                val imageRef = it.result?.imageRef
                FaceSample(template, imageRef)
            }
        }

        with(person) {
            assertThat(patientId, `is`(EXPECTED_GUID_FINGERPRINT))
            assertThat(projectId, `is`(request.projectId))
            assertThat(moduleId, `is`(request.moduleId))
            assertThat(userId, `is`(request.userId))
            assertThat(fingerprintSamples, `is`(emptyList())) // TODO: validate once implemented
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

    private fun mockFingerprintResponse() = FingerprintEnrolResponse(EXPECTED_GUID_FINGERPRINT)

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

    private companion object {
        const val EXPECTED_GUID_FINGERPRINT = "expected-guid"
    }

}
