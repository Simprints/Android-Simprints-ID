package com.simprints.eventsystem.sampledata

import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Fingerprint
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

object FingerprintCaptureEventV3Sample : SampleEvent() {

    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): FingerprintCaptureEventV3 {
        val fingerprint = Fingerprint(
            IFingerIdentifier.LEFT_THUMB,
            8,
            FingerprintTemplateFormat.ISO_19794_2
        )

        return FingerprintCaptureEventV3(
            CREATED_AT,
            ENDED_AT,
            IFingerIdentifier.LEFT_THUMB,
            10,
            FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Result.BAD_QUALITY,
            fingerprint,
            labels = labels
        )
    }

}
