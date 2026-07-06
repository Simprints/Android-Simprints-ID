package com.simprints.face.capture.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureConfirmationEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result
import com.simprints.infra.events.event.domain.models.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.FaceCaptureEvent.FaceCapturePayload
import com.simprints.infra.events.event.domain.models.FaceFallbackCaptureEvent
import com.simprints.infra.events.event.domain.models.FaceOnboardingCompleteEvent
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SimpleCaptureEventReporter @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    private val encodingUtils: EncodingUtils,
    @param:SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
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
            startTime = faceDetection.detectionStartTime,
            endTime = faceDetection.detectionEndTime,
            attemptNb = attempt,
            qualityThreshold = qualityThreshold,
            result = mapDetectionStatusToPayloadResult(faceDetection),
            isAutoCapture = isAutoCapture,
            isFallback = faceDetection.isFallback,
            face = mapDetectionToCapturePayloadFace(faceDetection = faceDetection),
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

    private fun mapDetectionToCapturePayloadFace(faceDetection: FaceDetection) = faceDetection.face?.let {
        FaceCapturePayload.Face(
            yaw = it.yaw,
            roll = it.roll,
            quality = it.quality,
            format = it.format,
            spoofScore = faceDetection.spoofCheckResult?.score,
            spoofSkipReason = mapSpoofReason(faceDetection.spoofCheckResult?.skipReason),
        )
    }

    private fun mapDetectionToCaptureBometricPayloadFace(faceDetection: FaceDetection) = faceDetection.face?.let {
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
            it.yaw,
            it.roll,
            encodingUtils.byteArrayToBase64(it.template),
            it.quality,
            it.format,
        )
    }!!

    private fun mapSpoofReason(reason: SpoofCheckResult.SkipReason?): FaceCapturePayload.SpoofSkipReason? = when (reason) {
        SpoofCheckResult.SkipReason.IMAGE_TOO_SMALL -> FaceCapturePayload.SpoofSkipReason.IMAGE_TOO_SMALL
        SpoofCheckResult.SkipReason.IOD_TOO_SMALL -> FaceCapturePayload.SpoofSkipReason.IOD_TOO_SMALL
        SpoofCheckResult.SkipReason.IOD_TOO_LARGE -> FaceCapturePayload.SpoofSkipReason.IOD_TOO_LARGE
        else -> null
    }

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
