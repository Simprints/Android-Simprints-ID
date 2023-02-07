package com.simprints.id.services.sync.events.master.internal

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.id.services.sync.events.master.internal.EventSyncCacheImpl.Companion.PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.*

class EventSyncCacheImplTest {

    companion object {
        private const val WORK_ID = "workID"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val sharedPrefsForProgresses = mockk<SharedPreferences>()
    private val sharedPrefsForLastSyncTime = mockk<SharedPreferences>()
    private val securityManager = mockk<SecurityManager> {
        every { buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_PROGRESSES_SHARED_PREFS) } returns sharedPrefsForProgresses
        every { buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS) } returns sharedPrefsForLastSyncTime
    }
    private val eventSyncCache = EventSyncCacheImpl(securityManager, testCoroutineRule.testCoroutineDispatcher)

    @Test
    fun `readLastSuccessfulSyncTime should return the date if it's greater than -1`() = runTest {
        every {
            sharedPrefsForLastSyncTime.getLong(
                PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY,
                -1
            )
        } returns 30

        val date = eventSyncCache.readLastSuccessfulSyncTime()
        assertThat(date).isEqualTo(Date(30))
    }

    @Test
    fun `readLastSuccessfulSyncTime should return the date if it's equal to -1`() = runTest {
        every {
            sharedPrefsForLastSyncTime.getLong(
                PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY,
                -1
            )
        } returns -1

        val date = eventSyncCache.readLastSuccessfulSyncTime()
        assertThat(date).isEqualTo(null)
    }

    @Test
    fun `storeLastSuccessfulSyncTime should store the date if it's not null`()  = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForLastSyncTime.edit() } returns editor

        eventSyncCache.storeLastSuccessfulSyncTime(Date(30))
        verify(exactly = 1) { editor.putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, 30) }
    }

    @Test
    fun `storeLastSuccessfulSyncTime should store -1 if the date is null`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForLastSyncTime.edit() } returns editor

        eventSyncCache.storeLastSuccessfulSyncTime(null)
        verify(exactly = 1) { editor.putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1) }
    }

    @Test
    fun `readProgress should read the progress for the worker`()  = runTest {
        every { sharedPrefsForProgresses.getInt(WORK_ID, 0) } returns 10

        val progress = eventSyncCache.readProgress(WORK_ID)
        assertThat(progress).isEqualTo(10)
    }

    @Test
    fun `saveProgress should save the progress for the worker`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForProgresses.edit() } returns editor

        eventSyncCache.saveProgress(WORK_ID, 10)
        verify(exactly = 1) { editor.putInt(WORK_ID, 10) }
    }

    @Test
    fun `clearProgresses should clear all the progresses for the workers`()  = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForProgresses.edit() } returns editor

        eventSyncCache.clearProgresses()
        verify(exactly = 1) { editor.clear() }
    }
}
