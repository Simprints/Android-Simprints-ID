package com.simprints.feature.debugdatagenerator.enrollmentrecords

import android.provider.ContactsContract.Directory.PACKAGE_NAME
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.face.capture.FaceCaptureResult.Sample
import com.simprints.feature.debugdatagenerator.enrollmentrecords.BiometricSdk.NEC
import com.simprints.feature.debugdatagenerator.enrollmentrecords.BiometricSdk.Roc1
import com.simprints.feature.debugdatagenerator.enrollmentrecords.BiometricSdk.Roc3
import com.simprints.feature.debugdatagenerator.enrollmentrecords.BiometricSdk.SimMatcher
import com.simprints.feature.orchestrator.usecases.CreatePersonEventUseCase
import com.simprints.feature.orchestrator.usecases.response.CreateEnrolResponseUseCase
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionConstants.ACTION_ENROL
import com.simprints.infra.orchestration.data.ActionRequest.EnrolActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import java.io.Serializable
import java.util.UUID
import javax.inject.Inject

class GenerateEnrollmentRecordsUseCase @Inject constructor(
    private val createEnrolResponse: CreateEnrolResponseUseCase,
    private val sessionEventRepository: SessionEventRepository,
    private val createPersonEvent: CreatePersonEventUseCase,
    private val timeHelper: TimeHelper,
    private val encodingUtils: EncodingUtils,
) {
    suspend operator fun invoke(
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        numberOfRecords: Int,
        biometricSdks: List<BiometricSdk>,
    ) {
        val request = EnrolActionRequest(
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            actionIdentifier = actionIdentifier,
            biometricDataSource = "",
            metadata = "",
            unknownExtras = emptyMap(),
        )
        repeat(numberOfRecords) {
            log("Generating enrollment record $it")
            sessionEventRepository.createSession()
            log("Created session")
            val captureResults = createBiometricCaptureResults(biometricSdks)
            log("Created capture results")
            createPersonEvent(captureResults)
            log("Created person event")
            createEnrolResponse(
                request = request,
                results = captureResults,
            )
            log("Created enrol response")
            sessionEventRepository.closeCurrentSession()
            log("Closed session")
        }
    }

    fun log(message: String) {
        Simber.tag("recordsgen").i(message)
    }

    private suspend fun createBiometricCaptureResults(biometricSdks: List<BiometricSdk>): List<Serializable> =
        biometricSdks.map { biometricSdk ->
            val detectionId = UUID.randomUUID().toString()

            val captureResults = when (biometricSdk) {
                is SimMatcher -> FingerprintCaptureResult(
                    results = biometricSdk.fingers.map { finger ->
                        FingerprintCaptureResult.Item(
                            captureEventId = detectionId,
                            identifier = finger,
                            sample = null,
                        )
                    },
                )

                is NEC -> FingerprintCaptureResult(
                    results = biometricSdk.fingers.map { finger ->
                        FingerprintCaptureResult.Item(
                            captureEventId = detectionId,
                            identifier = finger,
                            sample = null,
                        )
                    },
                )

                Roc1 -> FaceCaptureResult(
                    results = listOf(
                        FaceCaptureResult.Item(
                            captureEventId = detectionId,
                            index = 0,
                            sample = FaceCaptureResult.Sample(
                                faceId = "faceId",
                                template = byteArrayOf(0, 1, 2),
                                imageRef = null,
                                format = biometricSdk.format,
                            ),
                        ),
                    ),
                )

                Roc3 -> FaceCaptureResult(
                    results = listOf(
                        FaceCaptureResult.Item(
                            captureEventId = detectionId,
                            index = 0,
                            sample = FaceCaptureResult.Sample(
                                faceId = "faceId",
                                template = byteArrayOf(0, 1, 2),
                                imageRef = null,
                                format = biometricSdk.format,
                            ),
                        ),
                    ),
                )
            }
//        // insert the events in the session
            if (captureResults is FingerprintCaptureResult) {
                //  todo addFingerprintCaptureEvents(spec.format)
            } else if (captureResults is FaceCaptureResult) {
                addFaceCaptureEvents(biometricSdk.format, detectionId)
            }
            captureResults
        }

    private suspend fun addFaceCaptureEvents(
        format: String,
        detectionId: String,
    ) {
        val time = timeHelper.now()
        val faceCaptureEvent = FaceCaptureEvent(
            time,
            time,
            1,
            1.0f,
            FaceCapturePayload.Result.VALID,
            false,
            FaceCapturePayload.Face(
                1.0f,
                1.0f,
                1.0f,
                format,
            ),
            payloadId = detectionId,
        )

        sessionEventRepository.addOrUpdateEvent(faceCaptureEvent)

        sessionEventRepository.addOrUpdateEvent(
            FaceCaptureBiometricsEvent(
                time,
                FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
                    1.0f,
                    1.0f,
                    encodingUtils.byteArrayToBase64(byteArrayOf(0, 1, 2)),
                    1.0f,
                    format,
                ),
                payloadId = detectionId,
            ),
        )
    }

    companion object {
        val actionIdentifier = ActionRequestIdentifier(
            actionName = ACTION_ENROL,
            packageName = PACKAGE_NAME,
            callerPackageName = "com.simprints.feature.debugdatagenerator",
            contractVersion = 1,
            timestampMs = System.currentTimeMillis(),
        )
    }
}
