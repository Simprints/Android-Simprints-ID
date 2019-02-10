package com.simprints.id.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.TestApplication
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.db.local.room.getStatusId
import junit.framework.Assert.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DownSyncDaoTest {

    private var downSyncDao: DownSyncDao? = null
    private var db: SyncStatusDatabase? = null
    private val projectId = "projectId"
    private val userId = "userId"
    private val moduleId = "moduleId"
    private val lastPatientId = "lastPatientId"

    @Before
    fun createDb() {
        val app = ApplicationProvider.getApplicationContext() as TestApplication
        db = Room.inMemoryDatabaseBuilder(
            app, SyncStatusDatabase::class.java).build()
        downSyncDao = db?.downSyncDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeDownSyncStatus_shouldByInTheDb() {
        val downSyncStatus = DownSyncStatus(projectId, userId, moduleId, lastPatientId, 0, 0, 0)
        downSyncDao?.insertOrReplaceDownSyncStatus(downSyncStatus)
        val byId = downSyncDao?.getDownSyncStatusForId(downSyncDao?.getStatusId("projectId", "userId", "moduleId") ?: "")
        assertThat(byId).isEqualTo(downSyncStatus)
    }

    @Test
    @Throws(Exception::class)
    fun updatePeopleToDownSyncForId_shouldUpdateDownSyncCount() {
        val peopleToDownload = 200
        val downSyncStatus = DownSyncStatus(projectId, userId, moduleId, lastPatientId, 0, 0, 0)
        downSyncDao?.insertOrReplaceDownSyncStatus(downSyncStatus)
        downSyncDao?.updatePeopleToDownSync(downSyncDao!!.getStatusId(projectId, userId, moduleId), peopleToDownload)
        val byId = downSyncDao?.getDownSyncStatusForId(downSyncDao?.getStatusId(projectId, userId, moduleId) ?: "")
        assertThat(byId?.totalToDownload).isEqualTo(peopleToDownload)
    }

    @Test
    @Throws(Exception::class)
    fun updateLastSyncTimeForId_shouldUpdateLastSyncTime() {
        val lastSyncTime = 12345L
        val downSyncStatus = DownSyncStatus(projectId, userId, moduleId, lastPatientId, 0, 0, 0)
        downSyncDao?.insertOrReplaceDownSyncStatus(downSyncStatus)
        downSyncDao?.updateLastSyncTime(downSyncDao!!.getStatusId(projectId, userId, moduleId), lastSyncTime)
        val byId = downSyncDao?.getDownSyncStatusForId(downSyncDao?.getStatusId(projectId, userId, moduleId) ?: "")
        assertThat(byId?.lastSyncTime).isEqualTo(lastSyncTime)
    }

    @Test
    @Throws(Exception::class)
    fun updateLastPatientIdForId_shouldUpdateLastPatientId() {
        val newLastPatientId = "newLastPatientId"
        val downSyncStatus = DownSyncStatus(projectId, userId, moduleId, lastPatientId, 0, 0, 0)
        downSyncDao?.insertOrReplaceDownSyncStatus(downSyncStatus)
        downSyncDao?.updateLastPatientId(downSyncDao!!.getStatusId(projectId, userId, moduleId), newLastPatientId)
        val byId = downSyncDao?.getDownSyncStatusForId(downSyncDao?.getStatusId(projectId, userId, moduleId) ?: "")
        assertThat(byId?.lastPatientId).isEqualTo(newLastPatientId)
    }

    @Test
    @Throws(Exception::class)
    fun updateLastPatientUpdatedAtForId_shouldUpdateLastPatientUpdatedAt() {
        val newLastPatientUpdatedAt = 12345L
        val downSyncStatus = DownSyncStatus(projectId, userId, moduleId, lastPatientId, 0, 0, 0)
        downSyncDao?.insertOrReplaceDownSyncStatus(downSyncStatus)
        downSyncDao?.updateLastPatientUpdatedAt(downSyncDao!!.getStatusId(projectId, userId, moduleId), newLastPatientUpdatedAt)
        val byId = downSyncDao?.getDownSyncStatusForId(downSyncDao?.getStatusId(projectId, userId, moduleId) ?: "")
        assertThat(byId?.lastPatientUpdatedAt).isEqualTo(newLastPatientUpdatedAt)
    }

    @Test
    @Throws(Exception::class)
    fun getDownSyncStatusTest_shouldReturnNullLiveData() {
        val downSyncStatuses = downSyncDao?.getDownSyncStatusLiveData()
        assertNull(downSyncStatuses?.value)
    }
}
