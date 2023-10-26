package com.simprints.fingerprint.capture.usecase

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.randomUUID
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.FingerState
import com.simprints.fingerprint.capture.state.ScanResult
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result
import javax.inject.Inject

internal class AddCaptureEventsUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val encoder: EncodingUtils,
    private val eventRepository: EventRepository,
) {

    suspend operator fun invoke(
        lastCaptureStartedAt: Long,
        fingerState: FingerState,
        captureState: CaptureState,
        qualityThreshold: Int,
        tooManyBadScans: Boolean,
    ): String {
        val payloadId = randomUUID()

        val captureEvent = FingerprintCaptureEvent(
            createdAt = lastCaptureStartedAt,
            endTime = timeHelper.now(),
            finger = fingerState.id,
            qualityThreshold = qualityThreshold,
            result = mapCaptureStateToResult(captureState),
            fingerprint = mapCaptureStateToFingerprint(captureState, fingerState),
            payloadId = payloadId
        )

        val fingerprintCaptureBiometricsEvent =
            if (captureState is CaptureState.Collected && (captureEvent.payload.result == Result.GOOD_SCAN || tooManyBadScans))
                FingerprintCaptureBiometricsEvent(
                    createdAt = lastCaptureStartedAt,
                    fingerprint = mapCaptureToBiometricFingerprint(fingerState, captureState.scanResult),
                    payloadId = payloadId
                )
            else null

        eventRepository.addOrUpdateEvent(captureEvent)
        // Because we don't need biometric data that is not used for matching
        fingerprintCaptureBiometricsEvent?.let { eventRepository.addOrUpdateEvent(it) }

        return payloadId
    }


    private fun mapCaptureStateToResult(captureState: CaptureState) = when (captureState) {
        is CaptureState.Skipped -> Result.SKIPPED
        is CaptureState.NotDetected -> Result.NO_FINGER_DETECTED
        is CaptureState.Collected -> if (captureState.scanResult.isGoodScan()) {
            Result.GOOD_SCAN
        } else {
            Result.BAD_QUALITY
        }

        else -> Result.FAILURE_TO_ACQUIRE
    }

    private fun mapCaptureStateToFingerprint(captureState: CaptureState, fingerState: FingerState) = captureState
        .let { it as? CaptureState.Collected }
        ?.scanResult
        ?.let {
            FingerprintCapturePayload.Fingerprint(
                fingerState.id,
                it.qualityScore,
                it.templateFormat
            )
        }

    private fun mapCaptureToBiometricFingerprint(fingerState: FingerState, it: ScanResult) =
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
            finger = fingerState.id,
            quality = it.qualityScore,
            template = encoder.byteArrayToBase64(it.template),
            format = it.templateFormat
        )
}
