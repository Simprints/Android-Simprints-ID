package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_ENDED_AT
import com.simprints.id.data.db.event.domain.models.EventType.SCANNER_CONNECTION
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.Companion.EVENT_VERSION
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo
import com.simprints.id.sampledata.ScannerConnectionEventSample
import org.junit.Test

class ScannerConnectionEventTest {

    @Test
    fun create_ScannerConnectionEvent() {
        val labels = EventLabels(sessionId = GUID1)
        val scannerInfoArg = ScannerInfo("scanner_id", "mac_address", VERO_1, "hardware_version")
        val event = ScannerConnectionEventSample.getEvent()

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(SCANNER_CONNECTION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(DEFAULT_ENDED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SCANNER_CONNECTION)
            assertThat(scannerInfo).isEqualTo(scannerInfoArg)
        }
    }
}
