package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import com.simprints.id.sampledata.Vero2InfoSnapshotEventSample
import org.junit.Test

class Vero2InfoSnapshotEventTest {

    @Test
    fun create_Vero2InfoSnapshotEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val versionArg = Vero2Version(0, "cypressApp", "cypressApi", "stmApp", "stpApi", "un20App", "un20Api")
        val batteryArg = BatteryInfo(0, 1, 2, 3)
        val event = Vero2InfoSnapshotEventSample.getEvent()

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(VERO_2_INFO_SNAPSHOT)
        with(event.payload as Vero2InfoSnapshotPayload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(VERO_2_INFO_SNAPSHOT)
            assertThat(version).isEqualTo(versionArg)
            assertThat(battery).isEqualTo(batteryArg)
        }
    }
}

