package com.simprints.id.data.db.subjects_sync.down.local

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.subjects_sync.down.local.DbSubjectsDownSyncOperation.Converters.Companion.MODES_STRING_SEPARATOR
import org.junit.Test

class DbSubjectsDownSyncOperationTest {

    companion object {
        const val LAST_EVENT_ID = "lastEventId"
        const val LAST_SYNC_TIME = 2L
    }

    private val converter = DbSubjectsDownSyncOperation.Converters()
    private val subjectsDownSyncOperationKey = DbSubjectsDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, DEFAULT_USER_ID, DEFAULT_MODULE_ID)
    private val subjectsDownSyncOperationKeyAsString = "$DEFAULT_PROJECT_ID||$DEFAULT_USER_ID||$DEFAULT_MODULE_ID||${DEFAULT_MODES.joinToString("||")}"

    @Test
    fun testOpFromDomainToDb() {
        val dbKey = DbSubjectsDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, DEFAULT_USER_ID, DEFAULT_MODULE_ID)
        val dbOp = DbSubjectsDownSyncOperation(
            dbKey, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_MODES,
            COMPLETE,
            LAST_EVENT_ID,
            LAST_SYNC_TIME)

        with(dbOp.fromDbToDomain()) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(modes).isEqualTo(DEFAULT_MODES)
            assertThat(lastResult?.lastEventId).isEqualTo(LAST_EVENT_ID)
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
    fun testConverterStringToDbSubjectsDownSyncOperationKey() {
        val downSyncOpKeyString = converter.fromDbSubjectsDownSyncOperationKeyToString(subjectsDownSyncOperationKey)
        assertThat(downSyncOpKeyString).isEqualTo(subjectsDownSyncOperationKey.key)
    }

    @Test
    fun testConverterDbSubjectsDownSyncOperationKeyToString() {
        val downSyncOpKey = converter.fromStringToDbSubjectsDownSyncOperationKey(subjectsDownSyncOperationKeyAsString)
        assertThat(downSyncOpKey).isEqualTo(subjectsDownSyncOperationKey)
    }


    @Test
    fun testDbSubjectsDownSyncOperationKey() {
        assertThat(subjectsDownSyncOperationKey.key).isEqualTo(subjectsDownSyncOperationKeyAsString)
    }
}
