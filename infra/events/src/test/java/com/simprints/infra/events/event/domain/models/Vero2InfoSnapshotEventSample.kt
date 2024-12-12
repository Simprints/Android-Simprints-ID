package com.simprints.infra.events.event.domain.models

import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.BatteryInfo
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.Vero2Version
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT

object Vero2InfoSnapshotEventSample {
    fun getEvent(): Vero2InfoSnapshotEvent {
        val versionArg = Vero2Version.Vero2NewApiVersion(
            "E-1",
            "cypressApp",
            "stmApp",
            "un20App",
        )
        val batteryArg = BatteryInfo(0, 1, 2, 3)
        return Vero2InfoSnapshotEvent(CREATED_AT, versionArg, batteryArg)
    }

    val newApiJsonEventString =
        """
        {
            "id": "5bc59283-a448-4911-a21a-5d39b0e346a7",
            "scopeId": "af4eca90-c599-4323-97c7-c70e490c5568",
            "payload": {
                "createdAt": {"ms": 1234},
                "eventVersion": 3,
                "version": {
                    "hardwareRevision": "E-1",
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
                "endedAt": null
            },
            "type": "VERO_2_INFO_SNAPSHOT"
        }
        """.trimIndent()

    val oldApiJsonEventString =
        """
        {
            "id": "3afb1b9e-b263-4073-b773-6e1dac20d72f",
            "scopeId": "6dcb3810-4789-4149-8fea-473ffb520958",
            "payload": {
                "createdAt": {"ms": 1234},
                "eventVersion": 2,
                "version": {
                    "master": 10129,
                    "cypressApp": "1.1",
                    "cypressApi": "1.1",
                    "stmApp": "1.0",
                    "stmApi": "1.0",
                    "un20App": "1.2",
                    "un20Api": "1.2"
                },
                "battery": {
                    "charge": 0,
                    "voltage": 1,
                    "current": 2,
                    "temperature": 3
                },
                "type": "VERO_2_INFO_SNAPSHOT",
                "endedAt": null
            },
            "type": "VERO_2_INFO_SNAPSHOT"
        }
        """.trimIndent()
}
