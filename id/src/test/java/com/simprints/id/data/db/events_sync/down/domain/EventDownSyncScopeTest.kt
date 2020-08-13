package com.simprints.id.data.db.events_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.modulesSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.data.db.event.domain.models.EventType.*
import org.junit.Test

class EventDownSyncScopeTest {

    private val eventTypes = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_MOVE, ENROLMENT_RECORD_DELETION)

    @Test
    fun projectScopeBuild() {
        with(projectSyncScope.operations) {
            assertThat(this).hasSize(1)
            val op = this.first().queryEvent
            assertThat(op.projectId).isEqualTo(projectSyncScope.projectId)
            assertThat(op.modes).isEqualTo(projectSyncScope.modes)
            assertThat(op.types).isEqualTo(eventTypes)

            assertThat(op.subjectId).isNull()
            assertThat(op.attendantId).isNull()
            assertThat(op.moduleIds).isNull()
            assertThat(op.lastEventId).isNull()
        }
    }

    @Test
    fun userScopeBuild() {
        with(userSyncScope.operations) {
            assertThat(this).hasSize(1)
            val op = this.first().queryEvent
            assertThat(op.projectId).isEqualTo(projectSyncScope.projectId)
            assertThat(op.modes).isEqualTo(projectSyncScope.modes)
            assertThat(op.types).isEqualTo(eventTypes)
            assertThat(op.attendantId).isEqualTo(DEFAULT_USER_ID)

            assertThat(op.subjectId).isNull()
            assertThat(op.moduleIds).isNull()
            assertThat(op.lastEventId).isNull()
        }
    }

    @Test
    fun modulesScopeBuild() {
        with(modulesSyncScope.operations) {
            assertThat(this).hasSize(2)
            val op = this.first().queryEvent
            assertThat(op.projectId).isEqualTo(projectSyncScope.projectId)
            assertThat(op.modes).isEqualTo(projectSyncScope.modes)
            assertThat(op.types).isEqualTo(eventTypes)
            assertThat(op.moduleIds).isEqualTo(DEFAULT_MODULES)

            assertThat(op.subjectId).isNull()
            assertThat(op.attendantId).isNull()
            assertThat(op.lastEventId).isNull()
        }
    }
}
