package com.simprints.id.data.db.syncscope.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperationResult
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
import com.simprints.id.data.db.down_sync_info.domain.fromDomainToDb
import com.simprints.id.data.db.down_sync_info.local.DbDownSyncOperationKey
import com.simprints.id.data.db.down_sync_info.local.DownSyncOperationDao
import com.simprints.id.data.db.down_sync_info.local.SyncStatusDatabase
import com.simprints.id.data.db.down_sync_info.local.fromDbToDomain
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
    private lateinit var downSyncOperationDao: DownSyncOperationDao
    private lateinit var db: SyncStatusDatabase

    private val projectSyncOp = DownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val userSyncOp = DownSyncOperation(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        null,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        null
    )

    private val downSyncOperationResult: DownSyncOperationResult =
        DownSyncOperationResult(
            DownSyncOperationResult.DownSyncState.RUNNING,
            UUID.randomUUID().toString(),
            Date().time,
            Date().time
        )

    private val moduleSyncOp = DownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        DownSyncOperationResult(
            DownSyncOperationResult.DownSyncState.RUNNING,
            UUID.randomUUID().toString(),
            Date().time,
            Date().time
        )
    )

    private val moduleSyncOpFailed = DownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        DownSyncOperationResult(
            DownSyncOperationResult.DownSyncState.FAILED,
            null,
            null,
            Date().time
        )
    )


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, SyncStatusDatabase::class.java).build()
        downSyncOperationDao = db.downSyncOperationDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndReadProjectSyncOp() = runBlocking {
        assessSaveAndRead(projectSyncOp)
    }

    @Test
    fun writeUserAndReadUserSyncOp() = runBlocking {
        assessSaveAndRead(userSyncOp)
    }

    @Test
    fun writeUserAndReadModuleSyncOp() = runBlocking {
        assessSaveAndRead(moduleSyncOp)
    }

    @Test
    fun writeUserAndReadModuleFailedSyncOp() = runBlocking {
        assessSaveAndRead(moduleSyncOpFailed)
    }

    @Test
    fun insertOrReplaceDownSyncOperation() = runBlocking {
        downSyncOperationDao.insertOrReplaceDownSyncOperation(projectSyncOp.fromDomainToDb())
        val newOp = projectSyncOp.copy(syncOperationResult = downSyncOperationResult)
        downSyncOperationDao.insertOrReplaceDownSyncOperation(newOp.fromDomainToDb())
        val opStored = downSyncOperationDao.getDownSyncOperation(extractKeyFrom(newOp))
        assertThat(opStored).isEqualTo(newOp.fromDomainToDb())
    }

    private suspend fun assessSaveAndRead(downSyncOp: DownSyncOperation) {
        downSyncOperationDao.insertOrReplaceDownSyncOperation(downSyncOp.fromDomainToDb())
        val operation = downSyncOperationDao.getDownSyncOperation(extractKeyFrom(downSyncOp))
        assertThat(operation.fromDbToDomain()).isEqualTo(downSyncOp)
    }

    private fun extractKeyFrom(downSyncOp: DownSyncOperation): DbDownSyncOperationKey =
        with(downSyncOp) {
            DbDownSyncOperationKey(
                projectId,
                modes,
                userId,
                moduleId)
        }

}
