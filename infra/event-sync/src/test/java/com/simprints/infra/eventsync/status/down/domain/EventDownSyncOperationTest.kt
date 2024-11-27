package com.simprints.infra.eventsync.status.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.eventsync.SampleSyncScopes.modulesDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.userDownSyncScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.Companion.oldTypes
import org.junit.Test
import java.util.UUID

class EventDownSyncOperationTest {

    @Test
    fun eventDownSyncOperationForProjectScope_hasAnUniqueKey() {
        val op = projectDownSyncScope.operations.first()
        assertThat(op.getUniqueKey()).isEqualTo(
            uuidFrom(
                "${DEFAULT_PROJECT_ID}${DEFAULT_MODES.joinToString { it.name }}$oldTypes"
            )
        )
    }

    @Test
    fun eventDownSyncOperationForUserScope_hasAnUniqueKey() {
        val op = userDownSyncScope.operations.first()
        assertThat(op.getUniqueKey()).isEqualTo(
            uuidFrom(
                "$DEFAULT_PROJECT_ID${DEFAULT_USER_ID.value}${DEFAULT_MODES.joinToString { it.name }}$oldTypes"
            )
        )
    }

    @Test
    fun eventDownSyncOperationForModuleIdScope_hasAnUniqueKey() {
        val op = modulesDownSyncScope.operations.first()
        val op1 = modulesDownSyncScope.operations[1]

        assertThat(op.getUniqueKey()).isEqualTo(
            uuidFrom(
                "$DEFAULT_PROJECT_ID${DEFAULT_MODULE_ID.value}${DEFAULT_MODES.joinToString { it.name }}$oldTypes"
            )
        )

        assertThat(op1.getUniqueKey()).isEqualTo(
            uuidFrom(
                "$DEFAULT_PROJECT_ID${DEFAULT_MODULE_ID_2.value}${DEFAULT_MODES.joinToString { it.name }}$oldTypes"
            )
        )

    }

    private fun uuidFrom(seed: String): String =
        UUID.nameUUIDFromBytes(seed.toByteArray()).toString()
}
