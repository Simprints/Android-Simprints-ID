package com.simprints.id.data.db.people_sync.down.local

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.COMPLETE
import org.junit.Test

class DbPeopleDownSyncOperationTest {

    companion object {
        const val LAST_PATIENT_ID = "lastPatientId"
        const val LAST_PATIENT_UPDATED_AT = 1L
        const val LAST_SYNC_TIME = 2L

    }

    @Test
    fun testOpFromDomainToDb() {
        val dbKey = DbPeopleDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, DEFAULT_USER_ID, DEFAULT_MODULE_ID)
        val dbOp = DbPeopleDownSyncOperation(
            dbKey, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_MODES,
            COMPLETE,
            LAST_PATIENT_ID,
            LAST_PATIENT_UPDATED_AT,
            LAST_SYNC_TIME)

        with(dbOp.fromDbToDomain()) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(modes).isEqualTo(DEFAULT_MODES)
            assertThat(lastResult?.lastPatientUpdatedAt).isEqualTo(LAST_PATIENT_UPDATED_AT)
            assertThat(lastResult?.lastPatientId).isEqualTo(LAST_PATIENT_ID)
            assertThat(lastResult?.lastSyncTime).isEqualTo(LAST_SYNC_TIME)
            assertThat(lastResult?.state).isEqualTo(COMPLETE)
        }
    }

    @Test
    fun testDbPeopleDownSyncOperationKey(){
        val peopleDownSyncOpKey = DbPeopleDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, DEFAULT_USER_ID, DEFAULT_MODULE_ID)
        assertThat(peopleDownSyncOpKey.key).isEqualTo("$DEFAULT_PROJECT_ID||$DEFAULT_USER_ID||$DEFAULT_MODULE_ID||${DEFAULT_MODES.joinToString("||")}")
    }
}
