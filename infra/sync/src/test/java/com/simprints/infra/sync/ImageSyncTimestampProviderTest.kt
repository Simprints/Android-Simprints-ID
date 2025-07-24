package com.simprints.infra.sync

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.security.SecurityManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ImageSyncTimestampProviderTest {

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
        every { editor.clear() } returns editor

        imageSyncTimestampProvider = ImageSyncTimestampProvider(
            securityManager = securityManager,
            timeHelper = timeHelper,
        )
    }

    @Test
    fun `saveImageSyncCompletionTimestampNow saves current timestamp to secure preferences`() {
        val currentTime = 1234567890L
        every { timeHelper.now() } returns Timestamp(currentTime)

        imageSyncTimestampProvider.saveImageSyncCompletionTimestampNow()

        verify {
            sharedPreferences.edit()
            editor.putLong("IMAGE_SYNC_COMPLETION_TIME_MILLIS", currentTime)
        }
    }

    @Test
    fun `getSecondsSinceLastImageSync returns null when no timestamp exists`() {
        every { sharedPreferences.contains("IMAGE_SYNC_COMPLETION_TIME_MILLIS") } returns false
        every { sharedPreferences.getLong("IMAGE_SYNC_COMPLETION_TIME_MILLIS", 0) } returns 0

        val result = imageSyncTimestampProvider.getSecondsSinceLastImageSync()

        assertThat(result).isNull()
    }

    @Test
    fun `getSecondsSinceLastImageSync returns null when timestamp is zero`() {
        every { sharedPreferences.contains("IMAGE_SYNC_COMPLETION_TIME_MILLIS") } returns false
        every { sharedPreferences.getLong("IMAGE_SYNC_COMPLETION_TIME_MILLIS", 0) } returns 0

        val result = imageSyncTimestampProvider.getSecondsSinceLastImageSync()

        assertThat(result).isNull()
    }

    @Test
    fun `getSecondsSinceLastImageSync returns correct seconds when timestamp exists`() {
        val lastSyncTimeMillis = 1000000L
        val currentTimeMillis = 1005000L // 5 seconds later
        val expectedSeconds = 5L

        every { sharedPreferences.contains("IMAGE_SYNC_COMPLETION_TIME_MILLIS") } returns true
        every { sharedPreferences.getLong("IMAGE_SYNC_COMPLETION_TIME_MILLIS", 0) } returns lastSyncTimeMillis
        every { timeHelper.now() } returns Timestamp(currentTimeMillis)

        val result = imageSyncTimestampProvider.getSecondsSinceLastImageSync()

        assertThat(result).isEqualTo(expectedSeconds)
    }

    @Test
    fun `clearTimestamp clears all timestamp preferences`() {
        imageSyncTimestampProvider.clearTimestamp()

        val result = imageSyncTimestampProvider.getSecondsSinceLastImageSync()

        assertThat(result).isNull()
        verify {
            sharedPreferences.edit()
            editor.clear()
        }
    }

    @Test
    fun `provider uses correct secure preference file name`() {
        verify {
            securityManager.buildEncryptedSharedPreferences("93e98bc1-5b25-4805-94f6-f55ce0400747")
        }
    }
}
