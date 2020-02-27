package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.activities.collect.models.FingerStatus
import com.simprints.fingerprint.activities.collect.models.FingerStatus.*
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent as FingerprintCaptureEventCore
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent.Fingerprint as FingerprintCore
import com.simprints.id.data.db.session.domain.models.events.FingerprintCaptureEvent.Result as ResultCore
import com.simprints.id.data.db.person.domain.FingerIdentifier as FingerIdentifierCore

@Keep
class FingerprintCaptureEvent(starTime: Long,
                              endTime: Long,
                              val finger: FingerIdentifier,
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
        finger.fromDomainToCore(),
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

fun Fingerprint.fromDomainToCore() =
    FingerprintCaptureSample(fingerId.fromDomainToCore(), templateBytes, qualityScore)

fun FingerIdentifier.fromDomainToCore(): FingerIdentifierCore =
    when (this) {
        FingerIdentifier.RIGHT_5TH_FINGER -> FingerIdentifierCore.RIGHT_5TH_FINGER
        FingerIdentifier.RIGHT_4TH_FINGER -> FingerIdentifierCore.RIGHT_4TH_FINGER
        FingerIdentifier.RIGHT_3RD_FINGER -> FingerIdentifierCore.RIGHT_3RD_FINGER
        FingerIdentifier.RIGHT_INDEX_FINGER -> FingerIdentifierCore.RIGHT_INDEX_FINGER
        FingerIdentifier.RIGHT_THUMB -> FingerIdentifierCore.RIGHT_THUMB
        FingerIdentifier.LEFT_THUMB -> FingerIdentifierCore.LEFT_THUMB
        FingerIdentifier.LEFT_INDEX_FINGER -> FingerIdentifierCore.LEFT_INDEX_FINGER
        FingerIdentifier.LEFT_3RD_FINGER -> FingerIdentifierCore.LEFT_3RD_FINGER
        FingerIdentifier.LEFT_4TH_FINGER -> FingerIdentifierCore.LEFT_4TH_FINGER
        FingerIdentifier.LEFT_5TH_FINGER -> FingerIdentifierCore.LEFT_5TH_FINGER
    }
