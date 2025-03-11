package com.simprints.ear.capture.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.ear.capture.models.EarDetection
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result
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
        // TODO change to Ear event

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
        // TODO change to Ear event

        eventRepository.addOrUpdateEvent(FaceFallbackCaptureEvent(startTime, endTime))
    }

    suspend fun addCaptureEvents(
        faceDetection: EarDetection,
        attempt: Int,
        qualityThreshold: Float,
        isAutoCapture: Boolean = false,
    ) {
        // TODO
//        val faceCaptureEvent = FaceCaptureEvent(
//            faceDetection.detectionStartTime,
//            faceDetection.detectionEndTime,
//            attempt,
//            qualityThreshold,
//            mapDetectionStatusToPayloadResult(faceDetection),
//            isAutoCapture = isAutoCapture,
//            faceDetection.isFallback,
//
//            payloadId = faceDetection.id,
//        )
//
//        eventRepository.addOrUpdateEvent(faceCaptureEvent)
//        if (faceDetection.hasValidStatus()) {
//            eventRepository.addOrUpdateEvent(
//                FaceCaptureBiometricsEvent(
//                    faceDetection.detectionStartTime,
//
//                    payloadId = faceDetection.id,
//                ),
//            )
//        }
    }

    private fun mapDetectionStatusToPayloadResult(faceDetection: EarDetection) = when (faceDetection.status) {
        EarDetection.Status.VALID -> FaceCapturePayload.Result.VALID
        EarDetection.Status.VALID_CAPTURING -> FaceCapturePayload.Result.VALID
        EarDetection.Status.NO_EAR -> FaceCapturePayload.Result.INVALID
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
