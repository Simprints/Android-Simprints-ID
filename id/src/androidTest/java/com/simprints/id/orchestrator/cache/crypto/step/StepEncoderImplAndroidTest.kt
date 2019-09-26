package com.simprints.id.orchestrator.cache.crypto.step

import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.secure.keystore.KeystoreManagerImpl
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchingResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintTier
import com.simprints.id.orchestrator.cache.model.Fingerprint
import com.simprints.id.orchestrator.steps.Step
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.koin.core.context.stopKoin

class StepEncoderImplAndroidTest {

    private val stepEncoder by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val keystoreManager = KeystoreManagerImpl(context)
        StepEncoderImpl(keystoreManager)
    }

    @Test
    fun shouldEncodeFingerprintCaptureStepToString() {
        val fingerprintCaptureRequest = mockFingerprintCaptureRequest()
        val fingerprintCaptureResponse = mockFingerprintCaptureResponse()
        val step = buildStep(fingerprintCaptureRequest, fingerprintCaptureResponse)
        val encodedString = stepEncoder.encode(step)

        assertThat(encodedString, notNullValue())
        assertThat(encodedString, not(equalTo("")))
    }

    @Test
    fun shouldEncodeFaceCaptureStepToString() {
        val faceCaptureRequest = mockFaceCaptureRequest()
        val faceCaptureResponse = mockFaceCaptureResponse()
        val step = buildStep(faceCaptureRequest, faceCaptureResponse)
        val encodedString = stepEncoder.encode(step)

        assertThat(encodedString, notNullValue())
        assertThat(encodedString, not(equalTo("")))
    }

    @Test
    fun shouldDecodeStringToFingerprintCaptureStep() {
        val fingerprintCaptureRequest = mockFingerprintCaptureRequest()
        val fingerprintCaptureResponse = mockFingerprintCaptureResponse()
        val step = buildStep(fingerprintCaptureRequest, fingerprintCaptureResponse)
        val encodedString = stepEncoder.encode(step)
        val decodedStep = stepEncoder.decode(encodedString)

        with(decodedStep) {
            assertThat(requestCode, `is`(REQUEST_CODE))
            assertThat(activityName, `is`(ACTIVITY_NAME))
            assertThat(bundleKey, `is`(BUNDLE_KEY))
            assertThat(request, `is`(fingerprintCaptureRequest))
            assertThat(getStatus(), `is`(Step.Status.COMPLETED))
            assertThat(result, instanceOf(FingerprintCaptureResponse::class.java))
            require(result is FingerprintCaptureResponse)
            validateFingerprintCaptureResponse(result as FingerprintCaptureResponse,
                fingerprintCaptureResponse as FingerprintCaptureResponse)
        }
    }

    @Test
    fun shouldDecodeStringToFaceCaptureStep() {
        val faceCaptureRequest = mockFaceCaptureRequest()
        val faceCaptureResponse = mockFaceCaptureResponse()
        val step = buildStep(faceCaptureRequest, faceCaptureResponse)
        val encodedString = stepEncoder.encode(step)
        val decodedStep = stepEncoder.decode(encodedString)

        with(decodedStep) {
            assertThat(requestCode, `is`(REQUEST_CODE))
            assertThat(activityName, `is`(ACTIVITY_NAME))
            assertThat(bundleKey, `is`(BUNDLE_KEY))
            assertThat(request, `is`(faceCaptureRequest))
            assertThat(getStatus(), `is`(Step.Status.COMPLETED))
            assertThat(result, instanceOf(FaceCaptureResponse::class.java))
            validateFaceCaptureResponse(result as FaceCaptureResponse,
                faceCaptureResponse as FaceCaptureResponse)
        }
    }

    @Test
    fun shouldNotEncodeStepWithNonCaptureResult() {
        val fingerprintCaptureRequest = mockFingerprintCaptureRequest()
        val fingerprintIdentifyResponse = mockFingerprintIdentifyResponse()
        val step = buildStep(fingerprintCaptureRequest, fingerprintIdentifyResponse)
        val encodedString = stepEncoder.encode(step)
        val decodedStep = stepEncoder.decode(encodedString)

        with(decodedStep) {
            assertThat(requestCode, `is`(REQUEST_CODE))
            assertThat(activityName, `is`(ACTIVITY_NAME))
            assertThat(bundleKey, `is`(BUNDLE_KEY))
            assertThat(request, `is`(fingerprintCaptureRequest))
            assertThat(getStatus(), `is`(Step.Status.COMPLETED))
            assertThat(result, `is`(fingerprintIdentifyResponse))
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun buildStep(request: Step.Request, result: Step.Result): Step = Step(
        requestCode = REQUEST_CODE,
        activityName = ACTIVITY_NAME,
        bundleKey = BUNDLE_KEY,
        request = request,
        result = result,
        status = Step.Status.COMPLETED
    )

    private fun mockFingerprintCaptureRequest(): Step.Request = FingerprintCaptureRequest(
        "projectId",
        "userId",
        "moduleId",
        "metadata",
        "language",
        mapOf(),
        true,
        "programmeName",
        "organisationName",
        "activityTitle"
    )

    private fun mockFaceCaptureRequest(): Step.Request = FaceCaptureRequest(
        nFaceSamplesToCapture = 3
    )

    private fun mockFingerprintCaptureResponse(): Step.Result {
        val fingerprints = listOf(
            Fingerprint(
                IFingerIdentifier.RIGHT_THUMB,
                "template".toByteArray(),
                qualityScore = 3
            )
        )
        return FingerprintCaptureResponse(fingerprints)
    }

    private fun mockFaceCaptureResponse(): Step.Result {
        val template = "abcd1234".toByteArray()

        return FaceCaptureResponse(listOf(
            FaceCaptureResult(
                index = 1,
                result = FaceCaptureSample(
                    "face_id",
                    template,
                    SecuredImageRef("uri")
                )
            )
        ))
    }

    private fun mockFingerprintIdentifyResponse(): Step.Result {
        return FingerprintIdentifyResponse(
            listOf(
                FingerprintMatchingResult(
                    "guid-found",
                    3,
                    FingerprintTier.TIER_1
                )
            )
        )
    }

    private fun validateFingerprintCaptureResponse(actual: FingerprintCaptureResponse,
                                                   expected: FingerprintCaptureResponse) {
        with(actual) {
            fingerprints.forEachIndexed { index, actualFingerprint ->
                val expectedFingerprint = expected.fingerprints[index]
                assertThat(actualFingerprint.fingerId, `is`(expectedFingerprint.fingerId))
                assertThat(actualFingerprint.qualityScore, `is`(expectedFingerprint.qualityScore))
                assertThat(actualFingerprint.template.contentEquals(expectedFingerprint.template), `is`(true))
            }
        }
    }

    private fun validateFaceCaptureResponse(actual: FaceCaptureResponse,
                                            expected: FaceCaptureResponse) {
        with(actual) {
            capturingResult.forEachIndexed { index, item ->
                val expectedItem = expected.capturingResult[index]
                assertThat(item.index, `is`(expectedItem.index))

                item.result?.let { sample ->
                    val expectedSample = expectedItem.result
                    assertThat(sample.faceId, `is`(expectedSample?.faceId))
                    assertThat(sample.imageRef, `is`(expectedSample?.imageRef))
                    expectedSample?.template?.let { expectedTemplate ->
                        assertThat(sample.template.contentEquals(expectedTemplate), `is`(true))
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 123
        private const val ACTIVITY_NAME = "com.simprints.id.MyActivity"
        private const val BUNDLE_KEY = "BUNDLE_KEY"
    }

}
