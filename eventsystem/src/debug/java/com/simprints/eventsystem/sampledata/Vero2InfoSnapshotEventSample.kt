package com.simprints.eventsystem.sampledata

import com.simprints.core.tools.json.JsonHelper
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
        val versionArg = Vero2Version.Vero2NewApiVersion(
            "E-1",
            "cypressApp",
            "cypressApi",
            "stmApp"
        )
        val batteryArg = BatteryInfo(0, 1, 2, 3)
        return Vero2InfoSnapshotEvent(CREATED_AT, versionArg, batteryArg, labels)
    }

    val newApiJsonEventString = """
        {
            "id": "5bc59283-a448-4911-a21a-5d39b0e346a7",
            "labels": {
                "sessionId": "af4eca90-c599-4323-97c7-c70e490c5568"
            },
            "payload": {
                "createdAt": 1234,
                "eventVersion": 2,
                "version": {
                    "hardwareVersion": "E-1",
                    "cypressApp": "1.1",
                    "stmApp": "1.0",
                    "un20App": "1.2",
                    "eventVersion": 2
                },
                "battery": {
                    "charge": 0,
                    "voltage": 1,
                    "current": 2,
                    "temperature": 3
                },
                "type": "VERO_2_INFO_SNAPSHOT",
                "endedAt": 0
            },
            "type": "VERO_2_INFO_SNAPSHOT"
        }
    """.trimIndent()


    val oldApiJsonEventString = """
        {
            "id": "3afb1b9e-b263-4073-b773-6e1dac20d72f",
            "labels": {
                "sessionId": "6dcb3810-4789-4149-8fea-473ffb520958"
            },
            "payload": {
                "createdAt": 1234,
                "eventVersion": 1,
                "version": {
                    "master": 10129,
                    "cypressApp": "1.1",
                    "cypressApi": "1.1",
                    "stmApp": "1.0",
                    "stmApi": "1.0",
                    "un20App": "1.2",
                    "un20Api": "1.2",
                    "eventVersion": 1
                },
                "battery": {
                    "charge": 0,
                    "voltage": 1,
                    "current": 2,
                    "temperature": 3
                },
                "type": "VERO_2_INFO_SNAPSHOT",
                "endedAt": 0
            },
            "type": "VERO_2_INFO_SNAPSHOT"
        }
    """.trimIndent()

}
