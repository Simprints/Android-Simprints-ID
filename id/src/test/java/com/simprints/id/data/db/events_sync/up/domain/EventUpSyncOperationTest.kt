package com.simprints.id.data.db.events_sync.up.domain

import com.google.common.truth.Truth
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.DefaultTestConstants.projectUpSyncScope
import org.junit.Test
import java.util.*

class EventUpSyncOperationTest {

    @Test
    fun eventUpSyncOperationForProjectScope_hasAnUniqueKey() {
        val op = projectUpSyncScope.operation
        Truth.assertThat(op.getUniqueKey()).isEqualTo(UUID.nameUUIDFromBytes((DEFAULT_PROJECT_ID).toByteArray()).toString())
    }
}
