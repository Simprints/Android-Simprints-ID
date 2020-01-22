package com.simprints.id.services.scheduledSync.people.master.internal

import android.content.SharedPreferences
import com.simprints.id.tools.extensions.getMap
import com.simprints.id.tools.extensions.putMap
import com.simprints.id.tools.extensions.save
import java.util.*

class PeopleSyncCacheImpl(private val sharedPreferences: SharedPreferences) : PeopleSyncCache {

    private val editor = sharedPreferences.edit()

    override fun readLastSuccessfulSyncTime(): Date? {
        val dateLong = sharedPreferences.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
        return if (dateLong > -1) Date(dateLong) else null
    }

    override fun storeLastSuccessfulSyncTime(lastSyncTime: Date?) {
        editor.putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, lastSyncTime?.time ?: -1).apply()
    }

    override fun readProgress(workerId: String): Int =
        readAllProgresses()[workerId] ?: 0

    override fun saveProgress(workerId: String, progress: Int) {
        val progresses = readAllProgresses()
        progresses[workerId] = progress
        storeAllProgresses(progresses)
    }

    private fun readAllProgresses(): MutableMap<String, Int> =
        sharedPreferences
            .getMap(PEOPLE_SYNC_CACHE_PROGRESSES_KEY, emptyMap())
            .mapValues { it.value.toInt() }
            .toMutableMap()

    private fun storeAllProgresses(progresses: Map<String, Int>) {
        saveInSharedPrefs { it.putMap(PEOPLE_SYNC_CACHE_PROGRESSES_KEY, progresses.mapValues { it.value.toString() }) }
    }

    override fun clearProgresses() {
        saveInSharedPrefs { it.putMap(PEOPLE_SYNC_CACHE_PROGRESSES_KEY, emptyMap()) }
    }

    private fun saveInSharedPrefs(transaction: (SharedPreferences.Editor) -> Unit) {
        with(sharedPreferences) {
            save(transaction)
        }
    }

    companion object {
        const val PEOPLE_SYNC_CACHE_PROGRESSES_KEY = "PEOPLE_SYNC_CACHE_PROGRESSES_KEY"
        const val PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY = "PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY"
    }
}
