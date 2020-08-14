package com.simprints.id.data.db.events_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.modulesDownSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectDownSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userDownSyncScope
import com.simprints.id.data.db.event.domain.models.EventType.*
import org.junit.Test

class EventDownSyncScopeTest {

    private val eventTypes = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_MOVE, ENROLMENT_RECORD_DELETION)

    @Test
    fun projectScopeBuild() {
        with(projectDownSyncScope.operations) {
            assertThat(this).hasSize(1)
            val query = this.first().queryEvent
            assertThat(query.projectId).isEqualTo(projectDownSyncScope.projectId)
            assertThat(query.modes).isEqualTo(projectDownSyncScope.modes)
            assertThat(query.types).isEqualTo(eventTypes)

            assertThat(query.subjectId).isNull()
            assertThat(query.attendantId).isNull()
            assertThat(query.moduleIds).isNull()
            assertThat(query.lastEventId).isNull()
        }
    }

    @Test
    fun userScopeBuild() {
        with(userDownSyncScope.operations) {
            assertThat(this).hasSize(1)
            val query = this.first().queryEvent
            assertThat(query.projectId).isEqualTo(projectDownSyncScope.projectId)
            assertThat(query.modes).isEqualTo(projectDownSyncScope.modes)
            assertThat(query.types).isEqualTo(eventTypes)
            assertThat(query.attendantId).isEqualTo(DEFAULT_USER_ID)

            assertThat(query.subjectId).isNull()
            assertThat(query.moduleIds).isNull()
            assertThat(query.lastEventId).isNull()
        }
    }

    @Test
    fun modulesScopeBuild() {
        with(modulesDownSyncScope.operations) {
            assertThat(this).hasSize(2)
            val query = this.first().queryEvent
            checkModuleScope(query, DEFAULT_MODULE_ID)

            val query2 = this[2].queryEvent
            checkModuleScope(query2, DEFAULT_MODULE_ID_2)

        }
    }

    private fun checkModuleScope(op: RemoteEventQuery, moduleId: String) {
        assertThat(op.projectId).isEqualTo(projectDownSyncScope.projectId)
        assertThat(op.modes).isEqualTo(projectDownSyncScope.modes)
        assertThat(op.types).isEqualTo(eventTypes)
        assertThat(op.moduleIds).isEqualTo(listOf(moduleId))

        assertThat(op.subjectId).isNull()
        assertThat(op.attendantId).isNull()
        assertThat(op.lastEventId).isNull()
    }
}
