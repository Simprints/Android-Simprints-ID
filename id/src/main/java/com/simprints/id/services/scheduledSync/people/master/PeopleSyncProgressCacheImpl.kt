package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import android.content.SharedPreferences

class PeopleSyncProgressCacheImpl(ctx: Context) : PeopleSyncProgressCache {

    private val sharedPrefs: SharedPreferences = ctx.getSharedPreferences(WORKERS_PROGRESSES, Context.MODE_PRIVATE)

    override fun getProgress(workerId: String): Int =
        sharedPrefs.getInt(workerId, 0)

    override fun setProgress(workerId: String, progress: Int) {
        sharedPrefs.edit().putInt(workerId, progress).commit()
    }

    override fun clear() {
        sharedPrefs.edit().clear().apply()
    }

    private companion object {
        const val WORKERS_PROGRESSES = "WORKERS_PROGRESSES"
    }
}
