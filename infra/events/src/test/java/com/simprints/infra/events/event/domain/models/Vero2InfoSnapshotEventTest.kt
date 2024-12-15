package com.simprints.infra.events.event.domain.models

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.Companion.NEW_EVENT_VERSION
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class Vero2InfoSnapshotEventTest {
    @Test
    fun create_Vero2InfoSnapshotEvent() {
        val versionArg =
            Vero2InfoSnapshotEvent.Vero2Version.Vero2NewApiVersion(
                "E-1",
                "cypressApp",
                "stmApp",
                "un20App",
            )
        val batteryArg = Vero2InfoSnapshotEvent.BatteryInfo(0, 1, 2, 3)
        val event = Vero2InfoSnapshotEventSample.getEvent()

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(VERO_2_INFO_SNAPSHOT)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(NEW_EVENT_VERSION)
            assertThat(type).isEqualTo(VERO_2_INFO_SNAPSHOT)
            assertThat(version).isEqualTo(versionArg)
            assertThat(battery).isEqualTo(batteryArg)
        }
    }

    @Test
    fun shouldParseEvent_vero2Event_usingNewApi_successfully() {
        val versionArg = Vero2InfoSnapshotEvent.Vero2Version.Vero2NewApiVersion(
            hardwareRevision = "E-1",
            cypressApp = "1.1",
            stmApp = "1.0",
            un20App = "1.2",
        )
        val batteryArg = Vero2InfoSnapshotEvent.BatteryInfo(0, 1, 2, 3)
        val payload =
            Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi(
                CREATED_AT,
                NEW_EVENT_VERSION,
                batteryArg,
                versionArg,
            )
        val expectedEvent = Vero2InfoSnapshotEvent(
            id = "5bc59283-a448-4911-a21a-5d39b0e346a7",
            scopeId = "af4eca90-c599-4323-97c7-c70e490c5568",
            payload = payload,
            type = VERO_2_INFO_SNAPSHOT,
        )

        val eventAsString = Vero2InfoSnapshotEventSample.newApiJsonEventString
        val actualEvent = JsonHelper.fromJson(eventAsString, object : TypeReference<Event>() {})

        assertThat(expectedEvent).isEqualTo(actualEvent)
    }

    @Test
    fun shouldParse_vero2Event_usingOldApi_successfully() {
        val versionArg = Vero2InfoSnapshotEvent.Vero2Version.Vero2OldApiVersion(
            cypressApi = "1.1",
            cypressApp = "1.1",
            stmApi = "1.0",
            stmApp = "1.0",
            un20Api = "1.2",
            un20App = "1.2",
            master = 10129,
        )
        val batteryArg = Vero2InfoSnapshotEvent.BatteryInfo(0, 1, 2, 3)
        val payload = Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForOldApi(
            CREATED_AT,
            Vero2InfoSnapshotEvent.OLD_EVENT_VERSION,
            batteryArg,
            versionArg,
        )
        val expectedEvent = Vero2InfoSnapshotEvent(
            id = "3afb1b9e-b263-4073-b773-6e1dac20d72f",
            scopeId = "6dcb3810-4789-4149-8fea-473ffb520958",
            payload = payload,
            type = VERO_2_INFO_SNAPSHOT,
        )

        val eventAsString = Vero2InfoSnapshotEventSample.oldApiJsonEventString
        val actualEvent = JsonHelper.fromJson(eventAsString, object : TypeReference<Event>() {})

        assertThat(expectedEvent).isEqualTo(actualEvent)
    }
}
