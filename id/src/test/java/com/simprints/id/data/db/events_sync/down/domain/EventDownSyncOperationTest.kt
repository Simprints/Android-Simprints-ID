package com.simprints.id.data.db.events_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.sampledata.SampleDefaults.modulesDownSyncScope
import com.simprints.id.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.sampledata.SampleDefaults.userDownSyncScope
import com.simprints.id.data.db.event.domain.models.EventType.*
import org.junit.Test
import java.util.*

class EventDownSyncOperationTest {

    private val eventTypes = "$ENROLMENT_RECORD_CREATION, $ENROLMENT_RECORD_MOVE, $ENROLMENT_RECORD_DELETION"

    @Test
    fun eventDownSyncOperationForProjectScope_hasAnUniqueKey() {
        val op = projectDownSyncScope.operations.first()
        assertThat(op.getUniqueKey()).isEqualTo(uuidFrom(
            "${DEFAULT_PROJECT_ID}${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))
    }

    @Test
    fun eventDownSyncOperationForUserScope_hasAnUniqueKey() {
        val op = userDownSyncScope.operations.first()
        assertThat(op.getUniqueKey()).isEqualTo(uuidFrom(
        "$DEFAULT_PROJECT_ID$DEFAULT_USER_ID${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))
    }

    @Test
    fun eventDownSyncOperationForModuleIdScope_hasAnUniqueKey() {
        val op = modulesDownSyncScope.operations.first()
        val op1 = modulesDownSyncScope.operations[1]

        assertThat(op.getUniqueKey()).isEqualTo(uuidFrom(
            "$DEFAULT_PROJECT_ID$DEFAULT_MODULE_ID${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))

        assertThat(op1.getUniqueKey()).isEqualTo(uuidFrom(
            "$DEFAULT_PROJECT_ID$DEFAULT_MODULE_ID_2${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))

    }

    private fun uuidFrom(seed: String): String = UUID.nameUUIDFromBytes(seed.toByteArray()).toString()
}
