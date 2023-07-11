package com.simprints.id.orchestrator.cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.biometrics.FingerprintGeneratorUtils
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.testtools.common.mock.mockTemplate
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class StepEncoderImplTest {

    private val fakeSample = FingerprintGeneratorUtils.generateRandomFingerprint()
    private val stepEncoder = StepEncoderImpl()

    @Test
    fun shouldEncodeFingerprintCaptureStepToString() {
        val fingerprintCaptureRequest = mockFingerprintCaptureRequest()

        val fingerprintCaptureResponse = mockFingerprintCaptureResponse()
        val step = buildStep(fingerprintCaptureRequest, fingerprintCaptureResponse)
        val encodedString = stepEncoder.encode(step)

        MatcherAssert.assertThat(encodedString, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(encodedString, CoreMatchers.not(CoreMatchers.equalTo("")))
    }

    @Test
    fun shouldEncodeFaceCaptureStepToString() {
        val faceCaptureRequest = mockFaceCaptureRequest()
        val faceCaptureResponse = mockFaceCaptureResponse()
        val step = buildStep(faceCaptureRequest, faceCaptureResponse)
        val encodedString = stepEncoder.encode(step)

        MatcherAssert.assertThat(encodedString, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(encodedString, CoreMatchers.not(CoreMatchers.equalTo("")))
    }

    @Test
    fun shouldDecodeStringToFingerprintCaptureStep() {
        val fingerprintCaptureRequest = mockFingerprintCaptureRequest()
        val fingerprintCaptureResponse = mockFingerprintCaptureResponse()
        val step = buildStep(fingerprintCaptureRequest, fingerprintCaptureResponse)
        val encodedString = stepEncoder.encode(step)
        val decodedStep = stepEncoder.decode(encodedString)

        with(decodedStep) {
            MatcherAssert.assertThat(requestCode, CoreMatchers.`is`(REQUEST_CODE))
            MatcherAssert.assertThat(activityName, CoreMatchers.`is`(ACTIVITY_NAME))
            MatcherAssert.assertThat(bundleKey, CoreMatchers.`is`(BUNDLE_KEY))
            MatcherAssert.assertThat(payload, CoreMatchers.`is`(fingerprintCaptureRequest))
            MatcherAssert.assertThat(getStatus(), CoreMatchers.`is`(Step.Status.COMPLETED))
            MatcherAssert.assertThat(
                getResult(),
                CoreMatchers.instanceOf(FingerprintCaptureResponse::class.java)
            )
            require(getResult() is FingerprintCaptureResponse)
            validateFingerprintCaptureResponse(
                getResult() as FingerprintCaptureResponse,
                fingerprintCaptureResponse as FingerprintCaptureResponse
            )
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
            MatcherAssert.assertThat(requestCode, CoreMatchers.`is`(REQUEST_CODE))
            MatcherAssert.assertThat(activityName, CoreMatchers.`is`(ACTIVITY_NAME))
            MatcherAssert.assertThat(bundleKey, CoreMatchers.`is`(BUNDLE_KEY))
            MatcherAssert.assertThat(payload, CoreMatchers.`is`(faceCaptureRequest))
            MatcherAssert.assertThat(getStatus(), CoreMatchers.`is`(Step.Status.COMPLETED))
            MatcherAssert.assertThat(
                getResult(),
                CoreMatchers.instanceOf(FaceCaptureResponse::class.java)
            )
            validateFaceCaptureResponse(
                getResult() as FaceCaptureResponse,
                faceCaptureResponse as FaceCaptureResponse
            )
        }
    }

    private fun buildStep(request: Step.Request, result: Step.Result): Step = Step(
        requestCode = REQUEST_CODE,
        activityName = ACTIVITY_NAME,
        bundleKey = BUNDLE_KEY,
        payloadType = Step.PayloadType.REQUEST,
        payload = request,
        result = result,
        status = Step.Status.COMPLETED
    )

    private fun mockFingerprintCaptureRequest(): Step.Request = FingerprintCaptureRequest(
        fingerprintsToCapture = listOf(Finger.LEFT_THUMB, Finger.LEFT_INDEX_FINGER)
    )

    private fun mockFaceCaptureRequest(): Step.Request = FaceCaptureRequest(
        nFaceSamplesToCapture = 3
    )

    private fun mockFingerprintCaptureResponse(): Step.Result {
        val captureResult = listOf(
            FingerprintCaptureResult(
                Finger.RIGHT_THUMB,
                FingerprintCaptureSample(
                    Finger.RIGHT_THUMB,
                    fakeSample.template,
                    fakeSample.templateQualityScore,
                    FingerprintTemplateFormat.ISO_19794_2,
                    null
                )
            )
        )
        return FingerprintCaptureResponse(captureResult = captureResult)
    }

    private fun mockFaceCaptureResponse(): Step.Result {
        return FaceCaptureResponse(
            listOf(
                FaceCaptureResult(
                    index = 1,
                    result = FaceCaptureSample(
                        "face_id",
                        mockTemplate(),
                        null,
                        FACE_TEMPLATE_FORMAT
                    )
                )
            )
        )
    }


    private fun validateFingerprintCaptureResponse(
        actual: FingerprintCaptureResponse,
        expected: FingerprintCaptureResponse
    ) {
        with(actual) {
            captureResult.forEachIndexed { index, actualResult ->
                val expectedResult = expected.captureResult[index]
                MatcherAssert.assertThat(
                    actualResult.identifier,
                    CoreMatchers.`is`(expectedResult.identifier)
                )
                actualResult.sample?.let { actualSample ->
                    expectedResult.sample?.let { expectedSample ->
                        MatcherAssert.assertThat(
                            actualSample.fingerIdentifier,
                            CoreMatchers.`is`(expectedSample.fingerIdentifier)
                        )
                        MatcherAssert.assertThat(
                            actualSample.id,
                            CoreMatchers.`is`(expectedSample.id)
                        )
                        MatcherAssert.assertThat(
                            actualSample.imageRef,
                            CoreMatchers.`is`(expectedSample.imageRef)
                        )
                        MatcherAssert.assertThat(
                            actualSample.templateQualityScore,
                            CoreMatchers.`is`(expectedSample.templateQualityScore)
                        )
                        MatcherAssert.assertThat(
                            actualSample.template.contentEquals(expectedSample.template),
                            CoreMatchers.`is`(true)
                        )
                        MatcherAssert.assertThat(
                            actualSample.format,
                            CoreMatchers.`is`(FingerprintTemplateFormat.ISO_19794_2)
                        )
                    }
                }
            }
        }
    }

    private fun validateFaceCaptureResponse(
        actual: FaceCaptureResponse,
        expected: FaceCaptureResponse
    ) {
        with(actual) {
            capturingResult.forEachIndexed { index, item ->
                val expectedItem = expected.capturingResult[index]
                MatcherAssert.assertThat(item.index, CoreMatchers.`is`(expectedItem.index))

                item.result?.let { sample ->
                    val expectedSample = expectedItem.result
                    MatcherAssert.assertThat(
                        sample.faceId,
                        CoreMatchers.`is`(expectedSample?.faceId)
                    )
                    MatcherAssert.assertThat(
                        sample.imageRef,
                        CoreMatchers.`is`(expectedSample?.imageRef)
                    )
                    expectedSample?.template?.let { expectedTemplate ->
                        MatcherAssert.assertThat(
                            sample.template.contentEquals(expectedTemplate),
                            CoreMatchers.`is`(true)
                        )
                    }
                    MatcherAssert.assertThat(
                        sample.format,
                        CoreMatchers.`is`(FACE_TEMPLATE_FORMAT)
                    )
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 128
        private const val ACTIVITY_NAME = "com.simprints.id.MyActivity"
        private const val BUNDLE_KEY = "BUNDLE_KEY"
    }

}
