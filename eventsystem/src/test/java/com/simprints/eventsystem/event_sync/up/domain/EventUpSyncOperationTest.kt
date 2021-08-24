package com.simprints.eventsystem.event_sync.up.domain

import com.google.common.truth.Truth
import com.simprints.eventsystem.events_sync.up.domain.getUniqueKey
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.projectUpSyncScope
import org.junit.Test
import java.util.*

class EventUpSyncOperationTest {

    @Test
    fun eventUpSyncOperationForProjectScope_hasAnUniqueKey() {
        val op = projectUpSyncScope.operation
        Truth.assertThat(op.getUniqueKey()).isEqualTo(UUID.nameUUIDFromBytes((DEFAULT_PROJECT_ID).toByteArray()).toString())
    }
}