package com.simprints.infra.events.sampledata

import android.os.Build
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.Location
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.SampleDefaults.ENDED_AT

object SessionCaptureEventSample : SampleEvent() {

    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): SessionCaptureEvent {
        val appVersionNameArg = "appVersionName"
        val libSimprintsVersionNameArg = "libSimprintsVersionName"
        val languageArg = "language"
        val deviceArg = Device(
            Build.VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            labels.deviceId!!
        )

        val databaseInfoArg = DatabaseInfo(2, recordCount = 2)
        val locationArg = Location(0.0, 0.0)

        val event = SessionCaptureEvent(
            labels.sessionId!!,
            labels.projectId!!,
            CREATED_AT,
            listOf(Modality.FINGERPRINT, Modality.FACE),
            appVersionNameArg,
            libSimprintsVersionNameArg,
            languageArg,
            deviceArg,
            databaseInfoArg
        )
        event.payload.location = locationArg
        event.payload.endedAt = ENDED_AT
        event.payload.sessionIsClosed = isClosed

        return event
    }

}
