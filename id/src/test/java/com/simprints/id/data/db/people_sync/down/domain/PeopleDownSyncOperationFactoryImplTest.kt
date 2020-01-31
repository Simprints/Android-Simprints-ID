package com.simprints.id.data.db.people_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperationKey
import org.junit.Test

class PeopleDownSyncOperationFactoryImplTest {

    companion object {
        const val LAST_PATIENT_ID = "lastPatientId"
        const val LAST_PATIENT_UPDATED_AT = 1L
        const val LAST_SYNC_TIME = 2L
    }

    val result = PeopleDownSyncOperationResult(COMPLETE, LAST_PATIENT_ID, LAST_PATIENT_UPDATED_AT, LAST_SYNC_TIME)
    val builder = PeopleDownSyncOperationFactoryImpl()

    @Test
    fun testBuildProjectSyncOperation() {
        val op = builder.buildProjectSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_MODES, null)
        assertThat(op.projectId).isEqualTo(DEFAULT_PROJECT_ID)
        assertThat(op.userId).isNull()
        assertThat(op.moduleId).isNull()
        assertThat(op.modes).isEqualTo(DEFAULT_MODES)
        assertThat(op.lastResult).isNull()
    }

    @Test
    fun testBuildUserSyncOperation() {
        val op = builder.buildUserSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODES, null)
        assertThat(op.projectId).isEqualTo(DEFAULT_PROJECT_ID)
        assertThat(op.userId).isEqualTo(DEFAULT_USER_ID)
        assertThat(op.moduleId).isNull()
        assertThat(op.modes).isEqualTo(DEFAULT_MODES)
        assertThat(op.lastResult).isNull()
    }

    @Test
    fun testBuildModuleSyncOperation() {
        val op = builder.buildModuleSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_MODES, result)
        assertThat(op.projectId).isEqualTo(DEFAULT_PROJECT_ID)
        assertThat(op.userId).isNull()
        assertThat(op.moduleId).isEqualTo(DEFAULT_MODULE_ID)
        assertThat(op.modes).isEqualTo(DEFAULT_MODES)
        assertThat(op.lastResult).isEqualTo(result)
    }

    @Test
    fun testOpFromDomainToDb() {
        val op = builder.buildProjectSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_MODES, result)

        with(op.fromDomainToDb()) {
            assertThat(id.key).isEqualTo(DbPeopleDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, null).key)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isNull()
            assertThat(moduleId).isNull()
            assertThat(modes).isEqualTo(DEFAULT_MODES)
            assertThat(lastState).isEqualTo(COMPLETE)
            assertThat(lastPatientId).isEqualTo(LAST_PATIENT_ID)
            assertThat(lastPatientUpdatedAt).isEqualTo(LAST_PATIENT_UPDATED_AT)
            assertThat(lastSyncTime).isEqualTo(LAST_SYNC_TIME)
        }
    }
}
