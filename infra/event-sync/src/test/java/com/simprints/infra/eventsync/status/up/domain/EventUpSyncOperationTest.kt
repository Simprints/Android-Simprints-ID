package com.simprints.infra.eventsync.status.up.domain

import com.google.common.truth.Truth
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.eventsync.SampleSyncScopes.projectUpSyncScope
import org.junit.Test
import java.util.UUID

class EventUpSyncOperationTest {
    @Test
    fun eventUpSyncOperationForProjectScope_hasAnUniqueKey() {
        val op = projectUpSyncScope.operation
        Truth.assertThat(op.getUniqueKey()).isEqualTo(UUID.nameUUIDFromBytes((DEFAULT_PROJECT_ID).toByteArray()).toString())
    }
}
