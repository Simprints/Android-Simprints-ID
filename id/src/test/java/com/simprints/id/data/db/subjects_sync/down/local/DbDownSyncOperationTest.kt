package com.simprints.id.data.db.subjects_sync.down.local

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.subjects_sync.down.domain.EventsDownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.subjects_sync.down.local.DbEventsDownSyncOperation.Converters.Companion.MODES_STRING_SEPARATOR
import org.junit.Test

class DbEventDownSyncScopeRepoTest {

    companion object {
        const val LAST_EVENT_ID = "lastEventId"
        const val LAST_SYNC_TIME = 2L
    }

    private val converter = DbEventsDownSyncOperation.Converters()
    private val EventsDownSyncOperationKey = DbEventsDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, DEFAULT_USER_ID, DEFAULT_MODULE_ID)
    private val EventsDownSyncOperationKeyAsString = "$DEFAULT_PROJECT_ID||$DEFAULT_USER_ID||$DEFAULT_MODULE_ID||${DEFAULT_MODES.joinToString("||")}"

    @Test
    fun testOpFromDomainToDb() {
        val dbKey = DbEventsDownSyncOperationKey(DEFAULT_PROJECT_ID, DEFAULT_MODES, DEFAULT_USER_ID, DEFAULT_MODULE_ID)
        val dbOp = DbEventsDownSyncOperation(
            dbKey, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_MODES,
            COMPLETE,
            LAST_EVENT_ID,
            LAST_SYNC_TIME)

        with(dbOp.fromDbToDomain()) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID)
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
    fun testConverterStringToDbEventsDownSyncOperationKey() {
        val downSyncOpKeyString = converter.fromDbEventsDownSyncOperationKeyToString(EventsDownSyncOperationKey)
        assertThat(downSyncOpKeyString).isEqualTo(EventsDownSyncOperationKey.key)
    }

    @Test
    fun testConverterDbEventsDownSyncOperationKeyToString() {
        val downSyncOpKey = converter.fromStringToDbEventsDownSyncOperationKey(EventsDownSyncOperationKeyAsString)
        assertThat(downSyncOpKey).isEqualTo(EventsDownSyncOperationKey)
    }


    @Test
    fun testDbEventsDownSyncOperationKey() {
        assertThat(EventsDownSyncOperationKey.key).isEqualTo(EventsDownSyncOperationKeyAsString)
    }
}
