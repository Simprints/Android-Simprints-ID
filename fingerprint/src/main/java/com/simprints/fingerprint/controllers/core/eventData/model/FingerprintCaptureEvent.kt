package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent.Result
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent.Result.BAD_QUALITY
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent.Result.FAILURE_TO_ACQUIRE
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent.Result.GOOD_SCAN
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent.Result.NO_FINGER_DETECTED
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent.Result.SKIPPED
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToModuleApi
import com.simprints.infra.config.domain.models.Finger
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent as FingerprintCaptureEventCore
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint as FingerprintCore
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Result as ResultCore

/**
 * This class represents a Fingerprint Capture [Event].
 *
 * @property finger  the finger used for the capture, of this event
 * @property qualityThreshold  the fingerprint sample image quality threshold
 * @property result  the [Result] of the fingerprint capture
 * @property fingerprint the possible captured fingerprint
 */
@Keep
class FingerprintCaptureEvent(
    startTime: Long,
    endTime: Long,
    val finger: FingerIdentifier,
    val qualityThreshold: Int,
    val result: Result,
    val fingerprint: Fingerprint?,
    val payloadId: String
) : Event(EventType.FINGERPRINT_CAPTURE, startTime, endTime) {

    /**
     * The class represents the biometric data for the captured fingerprint event.
     *
     * @property finger  the finger identifier of the captured fingerprint
     * @property quality  the image quality of the captured fingerprint
     * @property template  the generated biometric template representing the fingerprint signature
     * @property format  the format for the template (i.e. the format used to generate the template
     *                   which will determine the format used to match fingerprints)
     */
    @Keep
    class Fingerprint(
        val finger: FingerIdentifier,
        val quality: Int,
        val format: FingerprintTemplateFormat
    )

    /**
     * This class represents the  quality result of a captured fingerprint
     *
     * - [GOOD_SCAN]  the resulting image was a good scan
     * - [BAD_QUALITY]  the resulting image scan is of bad quality
     * - [NO_FINGER_DETECTED]  no finger was detected during the fingerprint scan
     * - [SKIPPED]  a particular fingerprint scan was skipped: due to issues like a missing finger
     * - [FAILURE_TO_ACQUIRE]  the resulting image of the captured fingerprint wasn't successfully acquired from the scanner
     */
    @Keep
    enum class Result {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE;
    }

    companion object {
        // this function transforms a fingerprint capture status into a Result
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

fun FingerprintCaptureEvent.fromDomainToCore() = FingerprintCaptureEventCore(
    startTime,
    endTime,
    finger.fromDomainToModuleApi(),
    qualityThreshold,
    result.fromDomainToCore(),
    fingerprint?.fromDomainToCore(),
    payloadId = payloadId
)

fun FingerprintCaptureEvent.Fingerprint.fromDomainToCore() = FingerprintCore(
    finger.fromDomainToModuleApi(),
    quality,
    format
)

fun FingerprintCaptureEvent.Result.fromDomainToCore() = when (this) {
    FingerprintCaptureEvent.Result.GOOD_SCAN -> ResultCore.GOOD_SCAN
    FingerprintCaptureEvent.Result.BAD_QUALITY -> ResultCore.BAD_QUALITY
    FingerprintCaptureEvent.Result.NO_FINGER_DETECTED -> ResultCore.NO_FINGER_DETECTED
    FingerprintCaptureEvent.Result.SKIPPED -> ResultCore.SKIPPED
    FingerprintCaptureEvent.Result.FAILURE_TO_ACQUIRE -> ResultCore.FAILURE_TO_ACQUIRE
}

fun FingerIdentifier.fromDomainToCore(): Finger = when (this) {
    FingerIdentifier.RIGHT_5TH_FINGER -> Finger.RIGHT_5TH_FINGER
    FingerIdentifier.RIGHT_4TH_FINGER -> Finger.RIGHT_4TH_FINGER
    FingerIdentifier.RIGHT_3RD_FINGER -> Finger.RIGHT_3RD_FINGER
    FingerIdentifier.RIGHT_INDEX_FINGER -> Finger.RIGHT_INDEX_FINGER
    FingerIdentifier.RIGHT_THUMB -> Finger.RIGHT_THUMB
    FingerIdentifier.LEFT_THUMB -> Finger.LEFT_THUMB
    FingerIdentifier.LEFT_INDEX_FINGER -> Finger.LEFT_INDEX_FINGER
    FingerIdentifier.LEFT_3RD_FINGER -> Finger.LEFT_3RD_FINGER
    FingerIdentifier.LEFT_4TH_FINGER -> Finger.LEFT_4TH_FINGER
    FingerIdentifier.LEFT_5TH_FINGER -> Finger.LEFT_5TH_FINGER
}
