package com.simprints.id.data.db.subjects_sync.down.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.subjects_sync.SubjectsSyncStatusDatabase
import com.simprints.id.data.db.subjects_sync.down.local.DbEventsDownSyncOperationKey
import com.simprints.id.data.db.subjects_sync.down.local.EventDownSyncOperationLocalDataSource
import com.simprints.id.data.db.subjects_sync.down.local.fromDbToDomain
import com.simprints.id.domain.modality.Modes
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class EventSyncStatusDatabaseTest {
    private lateinit var downSyncOperationOperationDao: EventDownSyncOperationLocalDataSource
    private lateinit var db: SubjectsSyncStatusDatabase

    private val projectSyncOp = EventsDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val userSyncOp = EventsDownSyncOperation(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        null,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        null
    )

    private val downSyncOperationResult: EventsDownSyncOperationResult =
        EventsDownSyncOperationResult(
            EventsDownSyncOperationResult.DownSyncState.RUNNING,
            UUID.randomUUID().toString(),
            Date().time
        )

    private val moduleSyncOp = EventsDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        EventsDownSyncOperationResult(
            EventsDownSyncOperationResult.DownSyncState.RUNNING,
            UUID.randomUUID().toString(),
            Date().time
        )
    )

    private val moduleSyncOpFailed = EventsDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        EventsDownSyncOperationResult(
            EventsDownSyncOperationResult.DownSyncState.FAILED,
            null,
            Date().time
        )
    )


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, SubjectsSyncStatusDatabase::class.java).build()
        downSyncOperationOperationDao = db.downSyncOperationOperationDataSource
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndReadProjectSyncOp() = runBlocking {
        assertSaveAndRead(projectSyncOp)
    }

    @Test
    fun writeUserAndReadUserSyncOp() = runBlocking {
        assertSaveAndRead(userSyncOp)
    }

    @Test
    fun writeUserAndReadModuleSyncOp() = runBlocking {
        assertSaveAndRead(moduleSyncOp)
    }

    @Test
    fun writeUserAndReadModuleFailedSyncOp() = runBlocking {
        assertSaveAndRead(moduleSyncOpFailed)
    }

    @Test
    fun insertOrReplaceDownSyncOperation() = runBlocking {
        downSyncOperationOperationDao.insertOrReplaceDownSyncOperation(projectSyncOp.fromDomainToDb())
        val newOp = projectSyncOp.copy(lastResult = downSyncOperationResult)
        downSyncOperationOperationDao.insertOrReplaceDownSyncOperation(newOp.fromDomainToDb())
        val opStored = downSyncOperationOperationDao.getDownSyncOperation(extractKeyFrom(newOp))
        assertThat(opStored.first()).isEqualTo(newOp.fromDomainToDb())
    }

    private suspend fun assertSaveAndRead(downSyncOp: EventsDownSyncOperation) {
        downSyncOperationOperationDao.insertOrReplaceDownSyncOperation(downSyncOp.fromDomainToDb())
        val operation = downSyncOperationOperationDao.getDownSyncOperation(extractKeyFrom(downSyncOp))
        assertThat(operation.first().fromDbToDomain()).isEqualTo(downSyncOp)
    }

    private fun extractKeyFrom(downSyncOp: EventsDownSyncOperation): DbEventsDownSyncOperationKey =
        with(downSyncOp) {
            DbEventsDownSyncOperationKey(
                projectId,
                modes,
                attendantId,
                moduleId)
        }

}
