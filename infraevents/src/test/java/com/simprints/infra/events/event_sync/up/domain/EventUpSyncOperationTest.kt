package com.simprints.infra.events.event_sync.up.domain

import com.google.common.truth.Truth
import com.simprints.infra.events.events_sync.up.domain.getUniqueKey
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.projectUpSyncScope
import org.junit.Test
import java.util.*

class EventUpSyncOperationTest {

    @Test
    fun eventUpSyncOperationForProjectScope_hasAnUniqueKey() {
        val op = projectUpSyncScope.operation
        Truth.assertThat(op.getUniqueKey()).isEqualTo(UUID.nameUUIDFromBytes((DEFAULT_PROJECT_ID).toByteArray()).toString())
    }
}
