package com.simprints.infra.eventsync.sync.common

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.eventsync.sync.common.EventSyncCache.Companion.PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventSyncCacheTest {
    companion object {
        private const val WORK_ID = "workID"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var sharedPrefsForCount: SharedPreferences

    @MockK
    private lateinit var sharedPrefsForProgresses: SharedPreferences

    @MockK
    private lateinit var sharedPrefsForLastSyncTime: SharedPreferences

    @MockK
    private lateinit var securityManager: SecurityManager

    private lateinit var eventSyncCache: EventSyncCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { securityManager.buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_DOWN_COUNTS_SHARED_PREFS) } returns
            sharedPrefsForCount
        every { securityManager.buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_PROGRESSES_SHARED_PREFS) } returns
            sharedPrefsForProgresses
        every { securityManager.buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS) } returns
            sharedPrefsForLastSyncTime

        eventSyncCache = EventSyncCache(securityManager, testCoroutineRule.testCoroutineDispatcher)
    }

    @Test
    fun `readLastSuccessfulSyncTime should return the date if it's greater than -1`() = runTest {
        every {
            sharedPrefsForLastSyncTime.getLong(
                PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY,
                -1,
            )
        } returns 30

        val date = eventSyncCache.readLastSuccessfulSyncTime()
        assertThat(date).isEqualTo(Timestamp(30))
    }

    @Test
    fun `readLastSuccessfulSyncTime should return the date if it's equal to -1`() = runTest {
        every {
            sharedPrefsForLastSyncTime.getLong(
                PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY,
                -1,
            )
        } returns -1

        val date = eventSyncCache.readLastSuccessfulSyncTime()
        assertThat(date).isEqualTo(null)
    }

    @Test
    fun `storeLastSuccessfulSyncTime should store the date if it's not null`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForLastSyncTime.edit() } returns editor

        eventSyncCache.storeLastSuccessfulSyncTime(Timestamp(30))
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
    fun `readProgress should read the progress for the worker`() = runTest {
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
    fun `readMax should read the progress for the worker`() = runTest {
        every { sharedPrefsForCount.getInt(WORK_ID, 0) } returns 10

        val progress = eventSyncCache.readMax(WORK_ID)
        assertThat(progress).isEqualTo(10)
    }

    @Test
    fun `shouldIgnoreMax should return correct value`() = runTest {
        every { sharedPrefsForCount.getBoolean(any(), any()) } returns true
        assertThat(eventSyncCache.shouldIgnoreMax()).isTrue()
    }

    @Test
    fun `saveMax should save the progress for the worker`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForCount.edit() } returns editor

        eventSyncCache.saveMax(WORK_ID, 10)
        verify(exactly = 1) { editor.putInt(WORK_ID, 10) }
        verify(exactly = 0) { editor.putBoolean(any(), true) }
    }

    @Test
    fun `saveMax should set ignore max flag if provided null`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForCount.edit() } returns editor

        eventSyncCache.saveMax(WORK_ID, null)
        verify(exactly = 0) { editor.putInt(WORK_ID, 10) }
        verify(exactly = 1) { editor.putBoolean(any(), true) }
    }

    @Test
    fun `clearProgresses should clear all the progresses for the workers`() = runTest {
        val countEditor = mockk<SharedPreferences.Editor>(relaxed = true)
        val progressEditor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForCount.edit() } returns countEditor
        every { sharedPrefsForProgresses.edit() } returns progressEditor

        eventSyncCache.clearProgresses()
        verify(exactly = 1) {
            countEditor.clear()
            progressEditor.clear()
        }
    }
}
