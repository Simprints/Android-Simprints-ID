package com.simprints.id.services.scheduledSync.people.master.internal

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class PeopleSyncCacheImpl(ctx: Context) : PeopleSyncCache {

    private val sharedPrefs: SharedPreferences = ctx.getSharedPreferences(PEOPLE_SYNC_CACHE, Context.MODE_PRIVATE)
    private val edit = sharedPrefs.edit()

    override var lastSuccessfulSyncTime: Date? = null
        get() {
            val dateLong = sharedPrefs.getLong(KEY_LAST_SYNC_TIME, -1)
            return if (dateLong > -1) Date(dateLong) else null
        }
        set(value) {
            field = value
            edit.putLong(KEY_LAST_SYNC_TIME, -1).apply()
        }


    override fun readProgress(workerId: String): Int =
        sharedPrefs.getInt(workerId, 0)

    override fun saveProgress(workerId: String, progress: Int) {
        edit.putInt(workerId, progress).apply()
    }

    override fun clearProgresses() {
        edit.clear().apply()
    }

    override fun clearLastSyncTime() {
        lastSuccessfulSyncTime = null
    }

    private companion object {
        const val PEOPLE_SYNC_CACHE = "PEOPLE_SYNC_CACHE"
        private const val KEY_LAST_SYNC_TIME = "KEY_LAST_SYNC_TIME"

    }
}
