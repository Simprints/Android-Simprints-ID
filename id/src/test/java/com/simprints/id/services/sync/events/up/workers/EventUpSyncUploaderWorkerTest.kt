package com.simprints.id.services.sync.events.up.workers

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope
import org.junit.Test

class EventUpSyncUploaderWorkerTest {


    @Test
    fun eventUpSyncScope_canDeserializeOldFormat() {
        val jsonInput = """
        {
            "type": "EventUpSyncScope${'$'}ProjectScope",
            "projectId": "pcmqBbcaB4xWvfRHRELG",
            "operation": {
                "queryEvent": {
                    "projectId": "pcmqBbcaB4xWvfRHRELG"
                },
                "lastState": "FAILED",
                "lastSyncTime": 1620103325620
            }
        }    
        """.trimIndent()

        val expectedScope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
        expectedScope.operation.lastState = EventUpSyncOperation.UpSyncState.FAILED
        expectedScope.operation.lastSyncTime = 1620103325620

        val scope = EventUpSyncUploaderWorker.parseUpSyncInput(jsonInput)
        assertThat(scope).isEqualTo(expectedScope)
    }

    @Test
    fun eventUpSyncScope_canDeserializeNewFormat() {
        val jsonInput = """
        {
            "type": "EventUpSyncScope${'$'}ProjectScope",
            "projectId": "pcmqBbcaB4xWvfRHRELG",
            "operation": {
                "projectId": "pcmqBbcaB4xWvfRHRELG",
                "lastState": "FAILED",
                "lastSyncTime": 1620103325620
            }
        }
        """.trimIndent()

        val expectedScope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
        expectedScope.operation.lastState = EventUpSyncOperation.UpSyncState.FAILED
        expectedScope.operation.lastSyncTime = 1620103325620

        val scope = EventUpSyncUploaderWorker.parseUpSyncInput(jsonInput)
        assertThat(scope).isEqualTo(expectedScope)
    }

}
