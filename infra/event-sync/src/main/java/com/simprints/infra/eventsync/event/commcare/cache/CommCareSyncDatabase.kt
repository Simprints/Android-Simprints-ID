package com.simprints.infra.eventsync.event.commcare.cache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SyncedCaseEntity::class], version = 1, exportSchema = false)
abstract class CommCareSyncDatabase : RoomDatabase() {
    abstract fun commCareSyncDao(): CommCareSyncDao

    companion object {
        const val DATABASE_NAME = "commcare_sync_cache_db"
    }
}
