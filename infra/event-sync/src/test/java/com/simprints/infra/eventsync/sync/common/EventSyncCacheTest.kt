package com.simprints.infra.eventsync.sync.common

import android.content.SharedPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.sync.SyncFailureReason
import com.simprints.core.domain.sync.SyncOutcome
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.eventsync.sync.common.EventSyncCache.Companion.LAST_ATTEMPT_FAILURE_KEY
import com.simprints.infra.eventsync.sync.common.EventSyncCache.Companion.LAST_ATTEMPT_TIME_KEY
import com.simprints.infra.eventsync.sync.common.EventSyncCache.Companion.PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
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
    fun `the success time keeps the key it has always been stored under`() = runTest {
        // An upgrading device must carry it over rather than read as never synced, which would
        // drop every CommCare project straight into fallback.
        assertThat(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY).isEqualTo("PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY")
    }

    @Test
    fun `readLastSuccessfulSyncTime returns null until a sync has succeeded`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1) } returns -1

        assertThat(eventSyncCache.readLastSuccessfulSyncTime()).isNull()
    }

    @Test
    fun `readLastSuccessfulSyncTime returns the time of the last success, not the last attempt`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1) } returns 30
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns 90

        assertThat(eventSyncCache.readLastSuccessfulSyncTime()).isEqualTo(Timestamp(30))
    }

    @Test
    fun `readLastSyncOutcome returns null until a sync has been attempted`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns -1
        every { sharedPrefsForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1) } returns -1

        assertThat(eventSyncCache.readLastSyncOutcome()).isNull()
    }

    @Test
    fun `readLastSyncOutcome falls back to a success stored before attempts were recorded`() = runTest {
        // Upgrading from a version that only wrote the success key: without the fallback the
        // device would report never having synced until the next attempt.
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns -1
        every { sharedPrefsForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1) } returns 30

        assertThat(eventSyncCache.readLastSyncOutcome()).isEqualTo(SyncOutcome(Timestamp(30), failure = null))
    }

    @Test
    fun `readLastSyncOutcome prefers a recorded attempt over the legacy success`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns 90
        every { sharedPrefsForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1) } returns 30
        every { sharedPrefsForLastSyncTime.getString(LAST_ATTEMPT_FAILURE_KEY, null) } returns SyncFailureReason.CLOUD_INTEGRATION.name

        assertThat(eventSyncCache.readLastSyncOutcome())
            .isEqualTo(SyncOutcome(Timestamp(90), failure = SyncFailureReason.CLOUD_INTEGRATION))
    }

    @Test
    fun `readLastSyncOutcome returns the time of an attempt that succeeded`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns 30
        every { sharedPrefsForLastSyncTime.getString(LAST_ATTEMPT_FAILURE_KEY, null) } returns null

        assertThat(eventSyncCache.readLastSyncOutcome()).isEqualTo(SyncOutcome(Timestamp(30), failure = null))
    }

    @Test
    fun `readLastSyncOutcome returns the time and reason of an attempt that failed`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns 30
        every { sharedPrefsForLastSyncTime.getString(LAST_ATTEMPT_FAILURE_KEY, null) } returns "BACKEND_MAINTENANCE"

        assertThat(eventSyncCache.readLastSyncOutcome())
            .isEqualTo(SyncOutcome(Timestamp(30), SyncFailureReason.BACKEND_MAINTENANCE))
    }

    @Test
    fun `readLastSyncOutcome decodes a name from another version as unknown`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns 30
        every { sharedPrefsForLastSyncTime.getString(LAST_ATTEMPT_FAILURE_KEY, null) } returns "SOMETHING_ELSE"

        // A downgrade must not break reading the device state.
        assertThat(eventSyncCache.readLastSyncOutcome()?.failure).isEqualTo(SyncFailureReason.UNKNOWN)
    }

    @Test
    fun `storeLastSyncOutcome stores the time and the reason of a failure`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForLastSyncTime.edit() } returns editor

        eventSyncCache.storeLastSyncOutcome(Timestamp(30), SyncFailureReason.TOO_MANY_REQUESTS)

        verify(exactly = 1) { editor.putLong(LAST_ATTEMPT_TIME_KEY, 30) }
        verify(exactly = 1) { editor.putString(LAST_ATTEMPT_FAILURE_KEY, "TOO_MANY_REQUESTS") }
    }

    @Test
    fun `storeLastSyncOutcome clears a previous reason on success`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForLastSyncTime.edit() } returns editor

        eventSyncCache.storeLastSyncOutcome(Timestamp(30), null)

        verify(exactly = 1) { editor.putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, 30) }
        verify(exactly = 1) { editor.remove(LAST_ATTEMPT_FAILURE_KEY) }
        verify(exactly = 0) { editor.putString(LAST_ATTEMPT_FAILURE_KEY, any()) }
    }

    @Test
    fun `storeLastSyncOutcome moves the attempt time even when the sync failed`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForLastSyncTime.edit() } returns editor

        eventSyncCache.storeLastSyncOutcome(Timestamp(30), SyncFailureReason.CLOUD_INTEGRATION)

        // A device whose syncs keep failing must not look like one that stopped syncing.
        verify(exactly = 1) { editor.putLong(LAST_ATTEMPT_TIME_KEY, 30) }
    }

    @Test
    fun `clearLastSyncOutcome removes both keys`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefsForLastSyncTime.edit() } returns editor

        eventSyncCache.clearLastSyncOutcome()

        verify(exactly = 1) { editor.remove(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY) }
        verify(exactly = 1) { editor.remove(LAST_ATTEMPT_FAILURE_KEY) }
    }

    @Test
    fun `observeLastSyncOutcome starts from what is stored`() = runTest {
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns 30
        every { sharedPrefsForLastSyncTime.getString(LAST_ATTEMPT_FAILURE_KEY, null) } returns "TOO_MANY_REQUESTS"

        assertThat(eventSyncCache.observeLastSyncOutcome().first())
            .isEqualTo(SyncOutcome(Timestamp(30), SyncFailureReason.TOO_MANY_REQUESTS))
    }

    @Test
    fun `observeLastSyncOutcome publishes every write`() = runTest {
        every { sharedPrefsForLastSyncTime.edit() } returns mockk(relaxed = true)
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns -1
        every { sharedPrefsForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1) } returns -1

        eventSyncCache.observeLastSyncOutcome().test {
            assertThat(awaitItem()).isNull()

            // Encrypted preferences have no change notification of their own.
            eventSyncCache.storeLastSyncOutcome(Timestamp(30), SyncFailureReason.RELOGIN_REQUIRED)

            assertThat(awaitItem()).isEqualTo(SyncOutcome(Timestamp(30), SyncFailureReason.RELOGIN_REQUIRED))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeLastSyncOutcome publishes a clear`() = runTest {
        every { sharedPrefsForLastSyncTime.edit() } returns mockk(relaxed = true)
        every { sharedPrefsForLastSyncTime.getLong(LAST_ATTEMPT_TIME_KEY, -1) } returns 30
        every { sharedPrefsForLastSyncTime.getString(LAST_ATTEMPT_FAILURE_KEY, null) } returns null

        eventSyncCache.observeLastSyncOutcome().test {
            assertThat(awaitItem()).isNotNull()

            eventSyncCache.clearLastSyncOutcome()

            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
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
