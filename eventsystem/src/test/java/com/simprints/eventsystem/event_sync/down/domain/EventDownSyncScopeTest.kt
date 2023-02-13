package com.simprints.eventsystem.event_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.modulesDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.userDownSyncScope
import org.junit.Test

class EventDownSyncScopeTest {

    @Test
    fun projectScopeBuild() {
        with(projectDownSyncScope.operations) {
            assertThat(this).hasSize(1)
            val query = this.first().queryEvent
            assertThat(query.projectId).isEqualTo(projectDownSyncScope.projectId)
            assertThat(query.modes).isEqualTo(projectDownSyncScope.modes)

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

            val query2 = this[1].queryEvent
            checkModuleScope(query2, DEFAULT_MODULE_ID_2)

        }
    }

    private fun checkModuleScope(op: RemoteEventQuery, moduleId: String) {
        assertThat(op.projectId).isEqualTo(projectDownSyncScope.projectId)
        assertThat(op.modes).isEqualTo(projectDownSyncScope.modes)
        assertThat(op.moduleIds).isEqualTo(listOf(moduleId))

        assertThat(op.subjectId).isNull()
        assertThat(op.attendantId).isNull()
        assertThat(op.lastEventId).isNull()
    }
}
