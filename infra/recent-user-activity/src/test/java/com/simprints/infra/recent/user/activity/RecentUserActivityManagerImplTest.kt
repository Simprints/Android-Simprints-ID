package com.simprints.infra.recent.user.activity

import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.infra.recent.user.activity.local.RecentUserActivityLocalSource
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RecentUserActivityManagerImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val localSource = mockk<RecentUserActivityLocalSource>(relaxed = true)
    private lateinit var recentUserActivityManager: RecentUserActivityManagerImpl

    @Before
    fun setUp() {
        recentUserActivityManager = RecentUserActivityManagerImpl(localSource)
    }

    @Test
    fun `getRecentUserActivity should call the correct method`() = runTest {
        recentUserActivityManager.getRecentUserActivity()

        coVerify(exactly = 1) { localSource.getRecentUserActivity() }
    }

    @Test
    fun `updateRecentUserActivity should call the correct method`() = runTest {
        val update: (c: RecentUserActivity) -> RecentUserActivity = { it }
        recentUserActivityManager.updateRecentUserActivity(update)

        coVerify(exactly = 1) { localSource.updateRecentUserActivity(update) }
    }

    @Test
    fun `clearRecentActivity should call the correct method`() = runTest {
        recentUserActivityManager.clearRecentActivity()

        coVerify(exactly = 1) { localSource.clearRecentActivity() }
    }
}
