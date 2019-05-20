package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.activities.collect.models.FingerStatus
import com.simprints.fingerprint.activities.collect.models.FingerStatus.*
import com.simprints.fingerprint.activities.collect.models.fromDomainToCore
import com.simprints.id.data.analytics.eventdata.models.domain.events.FingerprintCaptureEvent as FingerprintCaptureEventCore
import com.simprints.id.data.analytics.eventdata.models.domain.events.FingerprintCaptureEvent.Fingerprint as FingerprintCore
import com.simprints.id.data.analytics.eventdata.models.domain.events.FingerprintCaptureEvent.Result as ResultCore

@Keep
class FingerprintCaptureEvent(starTime: Long,
                              endTime: Long,
                              val qualityThreshold: Int,
                              val result: Result,
                              val fingerprint: Fingerprint?) : Event(EventType.FINGERPRINT_CAPTURE, starTime, endTime) {

    @Keep
    class Fingerprint(val finger: FingerIdentifier,
                      val quality: Int, val template: String)

    @Keep
    enum class Result {
        GOOD_SCAN,
        BAD_QUALITY,
        NO_FINGER_DETECTED,
        SKIPPED,
        FAILURE_TO_ACQUIRE;
    }

    companion object {
        fun buildResult(status: FingerStatus): Result =
            when (status) {
                GOOD_SCAN, RESCAN_GOOD_SCAN -> Result.GOOD_SCAN
                BAD_SCAN -> Result.BAD_QUALITY
                NO_FINGER_DETECTED -> Result.NO_FINGER_DETECTED
                FINGER_SKIPPED -> Result.SKIPPED
                else -> Result.FAILURE_TO_ACQUIRE
            }

    }
}

fun FingerprintCaptureEvent.fromDomainToCore() =
    FingerprintCaptureEventCore(
        starTime,
        endTime,
        qualityThreshold,
        result.fromDomainToCore(),
        fingerprint?.fromDomainToCore())

fun FingerprintCaptureEvent.Fingerprint.fromDomainToCore() =
    FingerprintCore(finger.fromDomainToCore(), quality, template)

fun FingerprintCaptureEvent.Result.fromDomainToCore() =
    when (this) {
        FingerprintCaptureEvent.Result.GOOD_SCAN -> ResultCore.GOOD_SCAN
        FingerprintCaptureEvent.Result.BAD_QUALITY -> ResultCore.BAD_QUALITY
        FingerprintCaptureEvent.Result.NO_FINGER_DETECTED -> ResultCore.NO_FINGER_DETECTED
        FingerprintCaptureEvent.Result.SKIPPED -> ResultCore.SKIPPED
        FingerprintCaptureEvent.Result.FAILURE_TO_ACQUIRE -> ResultCore.FAILURE_TO_ACQUIRE
    }
