package com.simprints.infra.sync

import android.content.SharedPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.sync.SyncFailureReason
import com.simprints.core.domain.sync.SyncOutcome
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ImageSyncTimestampProviderTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    @MockK
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var imageSyncTimestampProvider: ImageSyncTimestampProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.clear() } returns editor
        storedTimestamp(null)
        storedSuccessTimestamp(null)
        every { sharedPreferences.getString(FAILURE_KEY, null) } returns null

        imageSyncTimestampProvider = ImageSyncTimestampProvider(
            securityManager = securityManager,
            timeHelper = timeHelper,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `getLastSyncOutcome returns null until an upload has been attempted`() = runTest {
        assertThat(imageSyncTimestampProvider.getLastSyncOutcome()).isNull()
    }

    @Test
    fun `getLastSyncOutcome falls back to a completion stored before attempts were recorded`() = runTest {
        // Upgrading from a version that only wrote the completion key: without the fallback the
        // device would report never having synced until the next attempt.
        storedSuccessTimestamp(1234567890L)

        assertThat(imageSyncTimestampProvider.getLastSyncOutcome())
            .isEqualTo(SyncOutcome(Timestamp(1234567890L), failure = null))
    }

    @Test
    fun `getLastSyncOutcome prefers a recorded attempt over the legacy completion`() = runTest {
        storedTimestamp(200L)
        storedSuccessTimestamp(100L)
        every { sharedPreferences.getString(FAILURE_KEY, null) } returns SyncFailureReason.CLOUD_INTEGRATION.name

        assertThat(imageSyncTimestampProvider.getLastSyncOutcome())
            .isEqualTo(SyncOutcome(Timestamp(200L), failure = SyncFailureReason.CLOUD_INTEGRATION))
    }

    @Test
    fun `getLastSyncOutcome returns the time of an attempt that succeeded`() = runTest {
        storedTimestamp(1234567890L)

        assertThat(imageSyncTimestampProvider.getLastSyncOutcome())
            .isEqualTo(SyncOutcome(Timestamp(1234567890L), failure = null))
    }

    @Test
    fun `getLastSyncOutcome returns the time and reason of an attempt that failed`() = runTest {
        storedTimestamp(1234567890L)
        every { sharedPreferences.getString(FAILURE_KEY, null) } returns "UNKNOWN"

        assertThat(imageSyncTimestampProvider.getLastSyncOutcome())
            .isEqualTo(SyncOutcome(Timestamp(1234567890L), SyncFailureReason.UNKNOWN))
    }

    @Test
    fun `getLastSyncOutcome decodes a name from another version as unknown`() = runTest {
        storedTimestamp(1234567890L)
        every { sharedPreferences.getString(FAILURE_KEY, null) } returns "SOMETHING_ELSE"

        assertThat(imageSyncTimestampProvider.getLastSyncOutcome()?.failure).isEqualTo(SyncFailureReason.UNKNOWN)
    }

    @Test
    fun `getMillisSinceLastImageSync returns null when nothing has succeeded`() = runTest {
        assertThat(imageSyncTimestampProvider.getMillisSinceLastImageSync()).isNull()
    }

    @Test
    fun `getMillisSinceLastImageSync measures from the last success, not the last attempt`() = runTest {
        storedSuccessTimestamp(1_000_000L)
        storedTimestamp(1_004_000L)
        every { timeHelper.now() } returns Timestamp(1_005_000L)

        assertThat(imageSyncTimestampProvider.getMillisSinceLastImageSync()).isEqualTo(5_000L)
    }

    @Test
    fun `getLastSuccessfulSyncTimestamp returns the last completed upload`() = runTest {
        storedSuccessTimestamp(1_000_000L)

        assertThat(imageSyncTimestampProvider.getLastSuccessfulSyncTimestamp()).isEqualTo(1_000_000L)
    }

    @Test
    fun `saveImageSyncOutcomeNow stores the time and the reason of a failure`() = runTest {
        every { timeHelper.now() } returns Timestamp(1234567890L)

        imageSyncTimestampProvider.saveImageSyncOutcomeNow(SyncFailureReason.UNKNOWN)

        verify {
            editor.putLong(ATTEMPT_KEY, 1234567890L)
            editor.putString(FAILURE_KEY, "UNKNOWN")
        }
        // A failing device must not read as freshly synced on the dashboard.
        verify(exactly = 0) { editor.putLong(SUCCESS_KEY, any()) }
    }

    @Test
    fun `saveImageSyncOutcomeNow clears a previous reason on success`() = runTest {
        every { timeHelper.now() } returns Timestamp(1234567890L)

        imageSyncTimestampProvider.saveImageSyncOutcomeNow(failure = null)

        verify {
            editor.putLong(ATTEMPT_KEY, 1234567890L)
            editor.putLong(SUCCESS_KEY, 1234567890L)
            editor.remove(FAILURE_KEY)
        }
    }

    @Test
    fun `the success time keeps the key it has always been stored under`() = runTest {
        every { timeHelper.now() } returns Timestamp(1234567890L)

        imageSyncTimestampProvider.saveImageSyncOutcomeNow(failure = null)

        // An upgrading device must carry it over rather than read as never synced.
        verify(exactly = 1) { editor.putLong("IMAGE_SYNC_COMPLETION_TIME_MILLIS", 1234567890L) }
    }

    @Test
    fun `observeLastSyncOutcome does not overwrite a value written while it was loading`() = runTest {
        storedTimestamp(30L)
        every { timeHelper.now() } returns Timestamp(90L)
        // The worker got there first; the stale read below must not win.
        imageSyncTimestampProvider.saveImageSyncOutcomeNow(SyncFailureReason.UNKNOWN)

        assertThat(imageSyncTimestampProvider.observeLastSyncOutcome().first())
            .isEqualTo(SyncOutcome(Timestamp(90L), SyncFailureReason.UNKNOWN))
    }

    @Test
    fun `observeLastSyncOutcome starts from what is stored`() = runTest {
        storedTimestamp(1234567890L)
        every { sharedPreferences.getString(FAILURE_KEY, null) } returns "RELOGIN_REQUIRED"

        imageSyncTimestampProvider.observeLastSyncOutcome().test {
            assertThat(awaitItem()).isEqualTo(SyncOutcome(Timestamp(1234567890L), SyncFailureReason.RELOGIN_REQUIRED))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeLastSyncOutcome publishes every write`() = runTest {
        every { timeHelper.now() } returns Timestamp(1234567890L)

        imageSyncTimestampProvider.observeLastSyncOutcome().test {
            assertThat(awaitItem()).isNull()

            // Encrypted preferences have no change notification of their own.
            imageSyncTimestampProvider.saveImageSyncOutcomeNow(SyncFailureReason.UNKNOWN)

            assertThat(awaitItem()).isEqualTo(SyncOutcome(Timestamp(1234567890L), SyncFailureReason.UNKNOWN))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearLastSyncOutcome wipes the store and publishes the clear`() = runTest {
        storedTimestamp(1234567890L)

        imageSyncTimestampProvider.observeLastSyncOutcome().test {
            assertThat(awaitItem()).isNotNull()

            imageSyncTimestampProvider.clearLastSyncOutcome()

            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
        verify { editor.clear() }
    }

    @Test
    fun `provider uses correct secure preference file name`() = runTest {
        imageSyncTimestampProvider.getLastSyncOutcome()

        verify {
            securityManager.buildEncryptedSharedPreferences("93e98bc1-5b25-4805-94f6-f55ce0400747")
        }
    }

    @Test
    fun `the encrypted file is not opened until something reads or writes it`() = runTest {
        // Opening it is a file operation too, so it has to wait for a call that runs on IO.
        verify(exactly = 0) { securityManager.buildEncryptedSharedPreferences(any()) }

        imageSyncTimestampProvider.getLastSyncOutcome()

        verify(exactly = 1) { securityManager.buildEncryptedSharedPreferences(any()) }
    }

    private fun storedTimestamp(ms: Long?) {
        every { sharedPreferences.contains(ATTEMPT_KEY) } returns (ms != null)
        every { sharedPreferences.getLong(ATTEMPT_KEY, 0) } returns (ms ?: 0)
    }

    private fun storedSuccessTimestamp(ms: Long?) {
        every { sharedPreferences.contains(SUCCESS_KEY) } returns (ms != null)
        every { sharedPreferences.getLong(SUCCESS_KEY, 0) } returns (ms ?: 0)
    }

    private companion object {
        const val ATTEMPT_KEY = "IMAGE_SYNC_ATTEMPT_TIME_MILLIS"
        const val SUCCESS_KEY = "IMAGE_SYNC_COMPLETION_TIME_MILLIS"
        const val FAILURE_KEY = "IMAGE_SYNC_FAILURE_REASON"
    }
}
