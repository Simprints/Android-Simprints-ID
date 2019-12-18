package com.simprints.id.data.db.syncscope.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.people_sync.PeopleSyncStatusDatabase
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult
import com.simprints.id.data.db.people_sync.down.domain.fromDomainToDb
import com.simprints.id.data.db.people_sync.down.local.DbDownSyncOperationKey
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncDao
import com.simprints.id.data.db.people_sync.down.local.fromDbToDomain
import com.simprints.id.domain.modality.Modes
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class SyncStatusDatabaseTest {
    private lateinit var downSyncOperationDao: PeopleDownSyncDao
    private lateinit var db: PeopleSyncStatusDatabase

    private val projectSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val userSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        null,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        null
    )

    private val downSyncOperationResult: PeopleDownSyncOperationResult =
        PeopleDownSyncOperationResult(
            PeopleDownSyncOperationResult.DownSyncState.RUNNING,
            UUID.randomUUID().toString(),
            Date().time,
            Date().time
        )

    private val moduleSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        PeopleDownSyncOperationResult(
            PeopleDownSyncOperationResult.DownSyncState.RUNNING,
            UUID.randomUUID().toString(),
            Date().time,
            Date().time
        )
    )

    private val moduleSyncOpFailed = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        PeopleDownSyncOperationResult(
            PeopleDownSyncOperationResult.DownSyncState.FAILED,
            null,
            null,
            Date().time
        )
    )


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, PeopleSyncStatusDatabase::class.java).build()
        downSyncOperationDao = db.downSyncOperationDao
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
        downSyncOperationDao.insertOrReplaceDownSyncOperation(projectSyncOp.fromDomainToDb())
        val newOp = projectSyncOp.copy(lastResult = downSyncOperationResult)
        downSyncOperationDao.insertOrReplaceDownSyncOperation(newOp.fromDomainToDb())
        val opStored = downSyncOperationDao.getDownSyncOperation(extractKeyFrom(newOp))
        assertThat(opStored).isEqualTo(newOp.fromDomainToDb())
    }

    private suspend fun assertSaveAndRead(downSyncOp: PeopleDownSyncOperation) {
        downSyncOperationDao.insertOrReplaceDownSyncOperation(downSyncOp.fromDomainToDb())
        val operation = downSyncOperationDao.getDownSyncOperation(extractKeyFrom(downSyncOp))
        assertThat(operation.first().fromDbToDomain()).isEqualTo(downSyncOp)
    }

    private fun extractKeyFrom(downSyncOp: PeopleDownSyncOperation): DbDownSyncOperationKey =
        with(downSyncOp) {
            DbDownSyncOperationKey(
                projectId,
                modes,
                userId,
                moduleId)
        }

}
