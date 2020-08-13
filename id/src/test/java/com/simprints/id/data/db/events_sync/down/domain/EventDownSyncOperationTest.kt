package com.simprints.id.data.db.events_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.modulesSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.data.db.event.domain.models.EventType.*
import org.junit.Test
import java.util.*

class EventDownSyncOperationTest {

    private val eventTypes = "$ENROLMENT_RECORD_CREATION, $ENROLMENT_RECORD_MOVE, $ENROLMENT_RECORD_DELETION"

    @Test
    fun eventDownSyncOperationForProjectScope_hasAnUniqueKey() {
        val op = projectSyncScope.operations.first()
        assertThat(op.getUniqueKey()).isEqualTo(uuidFrom(
            "${DEFAULT_PROJECT_ID}${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))
    }

    @Test
    fun eventDownSyncOperationForUserScope_hasAnUniqueKey() {
        val op = userSyncScope.operations.first()
        assertThat(op.getUniqueKey()).isEqualTo(uuidFrom(
        "$DEFAULT_PROJECT_ID$DEFAULT_USER_ID${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))
    }

    @Test
    fun eventDownSyncOperationForModuleIdScope_hasAnUniqueKey() {
        val op = modulesSyncScope.operations.first()
        val op1 = modulesSyncScope.operations[1]

        assertThat(op.getUniqueKey()).isEqualTo(uuidFrom(
            "$DEFAULT_PROJECT_ID$DEFAULT_MODULE_ID${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))

        assertThat(op1.getUniqueKey()).isEqualTo(uuidFrom(
            "$DEFAULT_PROJECT_ID$DEFAULT_MODULE_ID_2${DEFAULT_MODES.joinToString { it.name }}$eventTypes"))

    }

    private fun uuidFrom(seed: String): String = UUID.nameUUIDFromBytes(seed.toByteArray()).toString()
}
