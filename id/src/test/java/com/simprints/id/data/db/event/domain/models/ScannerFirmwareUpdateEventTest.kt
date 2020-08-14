package com.simprints.id.data.db.event.domain.models

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
import com.simprints.id.orchestrator.SOME_GUID1
import org.junit.Test

class ScannerFirmwareUpdateEventTest {

    @Test
    fun create_ScannerFirmwareUpdateEvent() {
        val chipArg = "chip"
        val targetVersionArg = "v1"
        val failureReasonArg = "error"

        val labels = EventLabels(sessionId = SOME_GUID1)
        val event = ScannerFirmwareUpdateEvent(CREATED_AT, ENDED_AT, chipArg, targetVersionArg, failureReasonArg, labels)

        assertThat(event.id).isNotNull()
        assertThat(event.labels).isEqualTo(labels)
        assertThat(event.type).isEqualTo(SCANNER_FIRMWARE_UPDATE)
        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(endedAt).isEqualTo(ENDED_AT)
            assertThat(eventVersion).isEqualTo(ScannerConnectionEvent.EVENT_VERSION)
            assertThat(type).isEqualTo(SCANNER_FIRMWARE_UPDATE)
            assertThat(chip).isEqualTo(chipArg)
            assertThat(targetAppVersion).isEqualTo(targetVersionArg)
            assertThat(failureReason).isEqualTo(failureReasonArg)
        }
    }
}

