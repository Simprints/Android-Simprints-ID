package com.simprints.id.data.local.room

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.id.Application
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DownSyncDaoTest {

    private var downSyncDao: DownSyncDao? = null
    private var db: SyncStatusDatabase? = null

    @Before
    fun createDb() {
        val app = InstrumentationRegistry.getTargetContext().applicationContext as Application
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
        val downSyncStatus = DownSyncStatus("projectId", "userId", "moduleId", "lastPatientId", 0, 0, 0)
        downSyncDao?.insertOrReplaceDownSyncStatus(downSyncStatus)
        val byId = downSyncDao?.getDownSyncStatusForId(downSyncDao?.getStatusId("projectId", "userId", "moduleId") ?: "")
        assertThat(byId).isEqualTo(downSyncStatus)
    }
}
