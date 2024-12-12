package com.simprints.infra.eventsync.status.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.eventsync.SampleSyncScopes.modulesDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.userDownSyncScope
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
            assertThat(query.moduleId).isNull()
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
            assertThat(query.attendantId).isEqualTo(DEFAULT_USER_ID.value)

            assertThat(query.subjectId).isNull()
            assertThat(query.moduleId).isNull()
            assertThat(query.lastEventId).isNull()
        }
    }

    @Test
    fun modulesScopeBuild() {
        with(modulesDownSyncScope.operations) {
            assertThat(this).hasSize(2)
            val query = this.first().queryEvent
            checkModuleScope(query, DEFAULT_MODULE_ID.value)

            val query2 = this[1].queryEvent
            checkModuleScope(query2, DEFAULT_MODULE_ID_2.value)
        }
    }

    private fun checkModuleScope(
        op: RemoteEventQuery,
        moduleId: String,
    ) {
        assertThat(op.projectId).isEqualTo(projectDownSyncScope.projectId)
        assertThat(op.modes).isEqualTo(projectDownSyncScope.modes)
        assertThat(op.moduleId).isEqualTo(moduleId)

        assertThat(op.subjectId).isNull()
        assertThat(op.attendantId).isNull()
        assertThat(op.lastEventId).isNull()
    }
}
