package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.testtools.common.syntax.assertThrows
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class PersonBuilderTest {

    private val personBuilder = AppResponseBuilderForEnrol.PersonBuilder

    @Test
    fun withFingerprintResponse_shouldBuildPerson() {
        val request = mockRequest()
        val fingerprintResponse = mockFingerprintResponse()

        val person = personBuilder.buildPerson(request, fingerprintResponse, null)

        assertThat(person, notNullValue())
        require(person != null)
        with(person) {
            assertThat(patientId, `is`(EXPECTED_GUID))
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

        val person = personBuilder.buildPerson(request, null, faceResponse)
        val expectedFaceSamples = faceResponse.capturingResult.map { it.result?.toDomain()!! }

        assertThat(person, notNullValue())
        require(person != null)
        with(person) {
            assertThat(patientId, `is`(EXPECTED_GUID))
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

        val person = personBuilder.buildPerson(request, fingerprintResponse, faceResponse)
        val expectedFaceSamples = faceResponse.capturingResult.map { it.result?.toDomain()!! }

        assertThat(person, notNullValue())
        require(person != null)
        with(person) {
            assertThat(patientId, `is`(EXPECTED_GUID))
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
    fun withNoResponses_shouldReturnNull() {
        val request = mockRequest()

        val person = personBuilder.buildPerson(request, null, null)

        assertThat(person, nullValue())
    }

    @Test
    fun withNullFaceSample_shouldThrowException() {
        val request = mockRequest()
        val faceResponse = mockFaceResponseWithNullSample()

        assertThrows<Throwable> {
            personBuilder.buildPerson(request, null, faceResponse)
        }
    }

    private fun mockRequest() = AppEnrolRequest(
        "projectId", "userId", "moduleId", "metadata"
    )

    private fun mockFingerprintResponse() = FingerprintEnrolResponse(EXPECTED_GUID)

    private fun mockFaceResponse(): FaceCaptureResponse {
        return FaceCaptureResponse(
            listOf(
                FaceCaptureResult(
                    index = 0,
                    result = FaceSample(EXPECTED_GUID, EXPECTED_GUID.toByteArray(), null)
                )
            )
        )
    }

    private fun mockFaceResponseWithNullSample(): FaceCaptureResponse {
        return FaceCaptureResponse(
            listOf(
                FaceCaptureResult(
                    index = 0,
                    result = null
                )
            )
        )
    }

    private companion object {
        const val EXPECTED_GUID = "expected-guid"
    }

}
