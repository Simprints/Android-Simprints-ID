package com.simprints.id.services.scheduledSync.people.master.internal

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCacheImpl.Companion.PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCacheImpl.Companion.PEOPLE_SYNC_CACHE_PROGRESSES_KEY
import com.simprints.id.tools.extensions.getMap
import com.simprints.id.tools.extensions.putMap
import com.simprints.id.tools.extensions.save
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
    private lateinit var sharedPrefs: SharedPreferences

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        sharedPrefs = ctx.getSharedPreferences("progress_cache", Context.MODE_PRIVATE)
        peopleSyncCache = PeopleSyncCacheImpl(sharedPrefs)
    }

    @Test
    fun cache_shouldStoreADownSyncWorkerProgress() {
        val progress = 1
        peopleSyncCache.saveProgress(workId, progress)
        with(readProgresses()) {
            assertThat(get(workId)).isEqualTo(progress)
            assertThat(size).isEqualTo(1)
        }
    }

    @Test
    fun cache_shouldReadDownSyncWorkerProgress() {
        val progress = 1
        storeProgresses(mapOf(workId to progress))
        val progressRead = peopleSyncCache.readProgress(workId)
        assertThat(progressRead).isEqualTo(progress)
    }

    @Test
    fun cache_shouldClearProgress() {
        storeProgresses(mapOf(workId to 1))
        peopleSyncCache.clearProgresses()
        assertThat(readProgresses().size).isEqualTo(0)
    }

    @Test
    fun cache_shouldStoreLastTime() {
        val now = Date()
        peopleSyncCache.lastSuccessfulSyncTime = now
        val stored = sharedPrefs.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, 0)
        assertThat(stored).isEqualTo(now.time)
    }

    @Test
    fun cache_shouldReadLastTime() {
        val now = Date()
        sharedPrefs.edit().putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, now.time).apply()

        val stored = peopleSyncCache.lastSuccessfulSyncTime

        assertThat(stored?.time).isEqualTo(now.time)
    }

    private fun readProgresses(): Map<String, Int> =
        sharedPrefs.getMap(PEOPLE_SYNC_CACHE_PROGRESSES_KEY, emptyMap()).mapValues { it.value.toInt() }

    private fun storeProgresses(progresses: Map<String, Int>) =
        sharedPrefs.save { it.putMap(PEOPLE_SYNC_CACHE_PROGRESSES_KEY, progresses.mapValues { it.value.toString() }) }
}
