package com.simprints.id.data.db.people_sync.down.local

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperation.Converters.Companion.MODES_STRING_SEPARATOR
import org.junit.Test

class DbPeopleDownSyncOperationTest {

    companion object {
        const val LAST_PATIENT_ID = "lastPatientId"
        const val LAST_PATIENT_UPDATED_AT = 1L
        const val LAST_SYNC_TIME = 2L
    }

    private val converter = DbPeopleDownSyncOperation.Converters()
    private val peopleDownSyncOperationKey = DbPeopleDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, DEFAULT_USER_ID, DEFAULT_MODULE_ID)
    private val peopleDownSyncOperationKeyAsString = "$DEFAULT_PROJECT_ID||$DEFAULT_USER_ID||$DEFAULT_MODULE_ID||${DEFAULT_MODES.joinToString("||")}"

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
    fun testConverterModesToString() {
        val modesInString = converter.fromModesToString(DEFAULT_MODES)
        assertThat(modesInString).isEqualTo(DEFAULT_MODES.joinToString(MODES_STRING_SEPARATOR))
    }

    @Test
    fun testConverterStringToModes() {
        val modes = converter.fromStringToModes(DEFAULT_MODES.joinToString(MODES_STRING_SEPARATOR))
        assertThat(modes).isEqualTo(DEFAULT_MODES)
    }

    @Test
    fun testConverterStringToDownSyncState() {
        val state = converter.fromStringToDownSyncState("COMPLETE")
        assertThat(state).isEqualTo(COMPLETE)
    }

    @Test
    fun testConverterDownSyncStateToString() {
        val stateString = converter.fromDownSyncStateToString(COMPLETE)
        assertThat(stateString).isEqualTo("COMPLETE")
    }

    @Test
    fun testConverterStringToDbPeopleDownSyncOperationKey() {
        val downSyncOpKeyString = converter.fromDbPeopleDownSyncOperationKeyToString(peopleDownSyncOperationKey)
        assertThat(downSyncOpKeyString).isEqualTo(peopleDownSyncOperationKey.key)
    }

    @Test
    fun testConverterDbPeopleDownSyncOperationKeyToString() {
        val downSyncOpKey = converter.fromStringToDbPeopleDownSyncOperationKey(peopleDownSyncOperationKeyAsString)
        assertThat(downSyncOpKey).isEqualTo(peopleDownSyncOperationKey)
    }


    @Test
    fun testDbPeopleDownSyncOperationKey() {
        assertThat(peopleDownSyncOperationKey.key).isEqualTo(peopleDownSyncOperationKeyAsString)
    }
}
