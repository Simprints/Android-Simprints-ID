package com.simprints.infra.config.sync

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
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

class ConfigSyncCacheTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var prefs: SharedPreferences

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var configSyncCache: ConfigSyncCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns prefs
        every { timeHelper.now() } returns Timestamp(1000)

        configSyncCache = ConfigSyncCache(
            securityManager,
            timeHelper,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `calling saveUpdateTime stores current timestamp to prefs`() = runTest {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { prefs.edit() } returns editor

        configSyncCache.saveUpdateTime()

        verify { editor.putLong(any(), 1000) }
    }

    @Test
    fun `calling sinceLastUpdateTime fetches timestamp from prefs`() = runTest {
        every { prefs.getLong(any(), any()) } returns 1000
        every { timeHelper.readableBetweenNowAndTime(any()) } returns "0 minutes"

        val result = configSyncCache.sinceLastUpdateTime()

        verify { prefs.getLong(any(), any()) }
        verify { timeHelper.readableBetweenNowAndTime(Timestamp(1000L)) }
        assertThat(result).isEqualTo("0 minutes")
    }

    @Test
    fun `returns empty stirng if no timestamp is found`() = runTest {
        every { prefs.getLong(any(), any()) } returns -1

        val result = configSyncCache.sinceLastUpdateTime()

        verify { prefs.getLong(any(), any()) }
        assertThat(result).isEmpty()
    }
}
