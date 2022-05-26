package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToModuleApi
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent as FingerprintCaptureBiometricsEventCore

@Keep
class FingerprintCaptureBiometricsEvent(
    createdAt: Long,
    endedAt: Long = 0,
    val result: Result,
    val fingerprint: Fingerprint?
) : Event(
    type = EventType.FINGERPRINT_CAPTURE_BIOMETRICS,
    startTime = createdAt,
    endTime = endedAt
) {

    @Keep
    class Fingerprint(
        val finger: FingerIdentifier,
        val quality: Int,
        val template: String,
        val format: FingerprintTemplateFormat = FingerprintTemplateFormat.ISO_19794_2
    )

    @Keep
    enum class Result {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE;
    }

    companion object {
        fun buildResult(status: CaptureState): Result = when (status) {
            is CaptureState.Skipped -> Result.SKIPPED
            is CaptureState.NotDetected -> Result.NO_FINGER_DETECTED
            is CaptureState.Collected -> if (status.scanResult.isGoodScan()) {
                Result.GOOD_SCAN
            } else {
                Result.BAD_QUALITY
            }
            else -> Result.FAILURE_TO_ACQUIRE
        }
    }
}

fun FingerprintCaptureBiometricsEvent.fromDomainToCore() = FingerprintCaptureBiometricsEventCore(
    createdAt = startTime,
    result = result.fromDomainToCore(),
    fingerprint = fingerprint?.fromDomainToCore()
)

fun FingerprintCaptureBiometricsEvent.Fingerprint.fromDomainToCore() =
    FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Fingerprint(
        finger.fromDomainToModuleApi(),
        template,
        format
    )

fun FingerprintCaptureBiometricsEvent.Result.fromDomainToCore() = when (this) {
    FingerprintCaptureBiometricsEvent.Result.GOOD_SCAN -> FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Result.GOOD_SCAN
    FingerprintCaptureBiometricsEvent.Result.BAD_QUALITY -> FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Result.BAD_QUALITY
    FingerprintCaptureBiometricsEvent.Result.NO_FINGER_DETECTED -> FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Result.NO_FINGER_DETECTED
    FingerprintCaptureBiometricsEvent.Result.SKIPPED -> FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Result.SKIPPED
    FingerprintCaptureBiometricsEvent.Result.FAILURE_TO_ACQUIRE -> FingerprintCaptureBiometricsEventCore.FingerprintCaptureBiometricsPayload.Result.FAILURE_TO_ACQUIRE
}
