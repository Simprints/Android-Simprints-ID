package com.simprints.id.data.db.events_sync.up.domain

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.events_sync.up.domain.old.EventUpSyncScope as OldEventUpSyncScope
import com.simprints.id.data.db.events_sync.up.domain.old.toNewScope
import org.junit.Test

class EventUpSyncScopeTest {

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

//        val scope = handleParsing(jsonInput)
//        assertThat(scope).isEqualTo(expectedScope)
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

//        val scope = handleParsing(jsonInput)
//        assertThat(scope).isEqualTo(expectedScope)
    }

}
