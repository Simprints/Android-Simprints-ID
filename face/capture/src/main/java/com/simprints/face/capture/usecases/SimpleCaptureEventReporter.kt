package com.simprints.face.capture.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.models.FaceDetection
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SimpleCaptureEventReporter @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    private val encodingUtils: EncodingUtils,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    fun addOnboardingCompleteEvent(startTime: Timestamp) = sessionCoroutineScope.launch {
        eventRepository.addOrUpdateEvent(FaceOnboardingCompleteEvent(startTime, timeHelper.now()))
    }

    fun addCaptureConfirmationEvent(
        startTime: Timestamp,
        isContinue: Boolean,
    ) = sessionCoroutineScope.launch {
        eventRepository.addOrUpdateEvent(
            FaceCaptureConfirmationEvent(
                startTime,
                timeHelper.now(),
                if (isContinue) Result.CONTINUE else Result.RECAPTURE,
            ),
        )
    }

    fun addFallbackCaptureEvent(
        startTime: Timestamp,
        endTime: Timestamp,
    ) = sessionCoroutineScope.launch {
        eventRepository.addOrUpdateEvent(FaceFallbackCaptureEvent(startTime, endTime))
    }

    suspend fun addCaptureEvents(
        faceDetection: FaceDetection,
        attempt: Int,
        qualityThreshold: Float,
        isAutoCapture: Boolean = false,
    ) {
        val faceCaptureEvent = FaceCaptureEvent(
            faceDetection.detectionStartTime,
            faceDetection.detectionEndTime,
            attempt,
            qualityThreshold,
            mapDetectionStatusToPayloadResult(faceDetection),
            isAutoCapture = isAutoCapture,
            faceDetection.isFallback,
            mapDetectionToCapturePayloadFace(faceDetection),
            payloadId = faceDetection.id,
        )

        eventRepository.addOrUpdateEvent(faceCaptureEvent)
        if (faceDetection.hasValidStatus()) {
            eventRepository.addOrUpdateEvent(
                FaceCaptureBiometricsEvent(
                    faceDetection.detectionStartTime,
                    mapDetectionToCaptureBometricPayloadFace(faceDetection),
                    payloadId = faceDetection.id,
                ),
            )
        }
    }

    private fun mapDetectionStatusToPayloadResult(faceDetection: FaceDetection) = when (faceDetection.status) {
        FaceDetection.Status.VALID -> FaceCapturePayload.Result.VALID
        FaceDetection.Status.VALID_CAPTURING -> FaceCapturePayload.Result.VALID
        FaceDetection.Status.NOFACE -> FaceCapturePayload.Result.INVALID
        FaceDetection.Status.BAD_QUALITY -> FaceCapturePayload.Result.BAD_QUALITY
        FaceDetection.Status.OFFYAW -> FaceCapturePayload.Result.OFF_YAW
        FaceDetection.Status.OFFROLL -> FaceCapturePayload.Result.OFF_ROLL
        FaceDetection.Status.TOOCLOSE -> FaceCapturePayload.Result.TOO_CLOSE
        FaceDetection.Status.TOOFAR -> FaceCapturePayload.Result.TOO_FAR
    }

    private fun mapDetectionToCapturePayloadFace(faceDetection: FaceDetection) =
        faceDetection.face?.let { FaceCapturePayload.Face(it.yaw, it.roll, it.quality, it.format) }

    private fun mapDetectionToCaptureBometricPayloadFace(faceDetection: FaceDetection) = faceDetection.face?.let {
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
            it.yaw,
            it.roll,
            encodingUtils.byteArrayToBase64(it.template),
            it.quality,
            it.format,
        )
    }!!

    fun addBiometricReferenceCreationEvents(
        referenceId: String,
        captureIds: List<String>,
    ) = sessionCoroutineScope.launch {
        eventRepository.addOrUpdateEvent(
            BiometricReferenceCreationEvent(
                startTime = timeHelper.now(),
                referenceId = referenceId,
                modality = BiometricReferenceCreationEvent.BiometricReferenceModality.FACE,
                captureIds = captureIds,
            ),
        )
    }
}
