package com.simprints.infra.events.sampledata

import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

object FingerprintCaptureEventSample : SampleEvent() {

    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): FingerprintCaptureEvent {
        val fingerprint = Fingerprint(
            IFingerIdentifier.LEFT_THUMB,
            8,
            "ISO_19794_2"
        )

        return FingerprintCaptureEvent(
            CREATED_AT,
            ENDED_AT,
            IFingerIdentifier.LEFT_THUMB,
            10,
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
            fingerprint,
            labels = labels
        )
    }

}
