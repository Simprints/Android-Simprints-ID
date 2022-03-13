package com.simprints.eventsystem.sampledata

import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT

object Vero2InfoSnapshotEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): Vero2InfoSnapshotEvent {
        val versionArg = Vero2Version(
            "E-1",
            "cypressApp",
            "cypressApi",
            "stmApp"
        )
        val batteryArg = BatteryInfo(0, 1, 2, 3)
        return Vero2InfoSnapshotEvent(CREATED_AT, versionArg, batteryArg, labels)
    }

}
