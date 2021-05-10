package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT

object Vero2InfoSnapshotEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): Vero2InfoSnapshotEvent {
        val versionArg = Vero2Version(
            0,
            "cypressApp",
            "cypressApi",
            "stmApp",
            "stpApi",
            "un20App",
            "un20Api"
        )
        val batteryArg = BatteryInfo(0, 1, 2, 3)
        return Vero2InfoSnapshotEvent(CREATED_AT, versionArg, batteryArg, labels)
    }

}
