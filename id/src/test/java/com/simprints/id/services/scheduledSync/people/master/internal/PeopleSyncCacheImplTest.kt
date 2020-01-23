package com.simprints.id.services.scheduledSync.people.master.internal

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCacheImpl.Companion.PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class PeopleSyncCacheImplTest {

    private val workId = UUID.randomUUID().toString()
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private lateinit var peopleSyncCache: PeopleSyncCache
    private lateinit var sharedPrefsForProgresses: SharedPreferences
    private lateinit var sharedPrefsForLastSyncTime: SharedPreferences

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        sharedPrefsForProgresses = ctx.getSharedPreferences("progress_cache", Context.MODE_PRIVATE)
        sharedPrefsForLastSyncTime = ctx.getSharedPreferences("lastSyncTime_cache", Context.MODE_PRIVATE)

        peopleSyncCache = PeopleSyncCacheImpl(sharedPrefsForProgresses, sharedPrefsForLastSyncTime)
    }

    @Test
    fun cache_shouldStoreADownSyncWorkerProgress() {
        val progress = 1
        peopleSyncCache.saveProgress(workId, progress)
        assertThat(sharedPrefsForProgresses.getInt(workId, 0)).isEqualTo(1)
    }

    @Test
    fun cache_shouldReadDownSyncWorkerProgress() {
        val progress = 1
        storeProgresses(workId, progress)
        val progressRead = peopleSyncCache.readProgress(workId)
        assertThat(progressRead).isEqualTo(progress)
    }

    @Test
    fun cache_shouldClearProgress() {
        storeProgresses(workId, 1)
        peopleSyncCache.clearProgresses()
        assertThat(sharedPrefsForProgresses.all.size).isEqualTo(0)
    }

    @Test
    fun cache_shouldStoreLastTime() {
        val now = Date()
        peopleSyncCache.storeLastSuccessfulSyncTime(now)
        val stored = sharedPrefsForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, 0)
        assertThat(stored).isEqualTo(now.time)
    }

    @Test
    fun cache_shouldReadLastTime() {
        val now = Date()
        sharedPrefsForLastSyncTime.edit().putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, now.time).apply()

        val stored = peopleSyncCache.readLastSuccessfulSyncTime()

        assertThat(stored?.time).isEqualTo(now.time)
    }

    private fun storeProgresses(workInfo: String, progress: Int) =
        sharedPrefsForProgresses.edit().putInt(workInfo, progress).commit()
}
