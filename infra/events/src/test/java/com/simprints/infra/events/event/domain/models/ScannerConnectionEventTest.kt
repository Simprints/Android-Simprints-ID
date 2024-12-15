package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.EventType.SCANNER_CONNECTION
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.Companion.EVENT_VERSION
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class ScannerConnectionEventTest {
    @Test
    fun create_ScannerConnectionEvent() {
        val scannerInfoArg = ScannerInfo("scanner_id", "mac_address", VERO_1, "hardware_version")
        val event = ScannerConnectionEvent(
            CREATED_AT,
            ScannerInfo(
                "scanner_id",
                "mac_address",
                ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1,
                "hardware_version",
            ),
        )

        assertThat(event.id).isNotNull()
        assertThat(event.type).isEqualTo(SCANNER_CONNECTION)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(eventVersion).isEqualTo(EVENT_VERSION)
            assertThat(type).isEqualTo(SCANNER_CONNECTION)
            assertThat(scannerInfo).isEqualTo(scannerInfoArg)
        }
    }
}
