package com.simprints.eventsystem.sampledata

import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

object FingerprintCaptureEventSample : SampleEvent() {

    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): FingerprintCaptureEvent {
        val fingerprint = Fingerprint(
            IFingerIdentifier.LEFT_THUMB,
            8,
            "template",
            FingerprintTemplateFormat.ISO_19794_2
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
