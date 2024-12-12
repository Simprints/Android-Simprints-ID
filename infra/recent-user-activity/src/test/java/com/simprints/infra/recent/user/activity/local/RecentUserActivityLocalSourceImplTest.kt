package com.simprints.infra.recent.user.activity.local

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentUserActivityLocalSourceImplTest {
    companion object {
        private const val TEST_DATASTORE_NAME: String = "test_datastore"
    }

    private val testContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val testDataStore = DataStoreFactory.create(
        serializer = RecentUserActivitySerializer,
        produceFile = { testContext.dataStoreFile(TEST_DATASTORE_NAME) },
    )
    private val timeHelper = mockk<TimeHelper>(relaxed = true)
    private val recentUserActivityLocalSourceImpl =
        RecentUserActivityLocalSourceImpl(testDataStore, timeHelper)

    @Test
    fun `should clear the old activity before returning the recent user activity`() = runTest {
        val user = "user".asTokenizableEncrypted()
        every { timeHelper.now() } returns Timestamp(20000L)
        recentUserActivityLocalSourceImpl.updateRecentUserActivity {
            it.apply {
                it.lastUserUsed = user
                it.enrolmentsToday = 10
                it.lastActivityTime = 10000
            }
        }
        val recentActivity = recentUserActivityLocalSourceImpl.getRecentUserActivity()
        assertThat(recentActivity.enrolmentsToday).isEqualTo(0)
        assertThat(recentActivity.lastActivityTime).isEqualTo(10000)
        assertThat(recentActivity.lastUserUsed).isEqualTo(user)
    }

    @Test
    fun `should clear the old activity before updating the recent user activity`() = runTest {
        val user = "user".asTokenizableEncrypted()
        every { timeHelper.now() } returnsMany listOf(Timestamp(0L), Timestamp(11000L))
        every { timeHelper.tomorrowInMillis() } returns 20000
        recentUserActivityLocalSourceImpl.updateRecentUserActivity {
            it.apply {
                it.lastUserUsed = user
                it.enrolmentsToday = 10
                it.lastActivityTime = 10000
            }
        }

        recentUserActivityLocalSourceImpl.updateRecentUserActivity {
            it.apply {
                it.enrolmentsToday = 1
                it.lastActivityTime = 12000
            }
        }
        val recentActivity = recentUserActivityLocalSourceImpl.getRecentUserActivity()
        assertThat(recentActivity.enrolmentsToday).isEqualTo(1)
        assertThat(recentActivity.lastActivityTime).isEqualTo(12000)
        assertThat(recentActivity.lastUserUsed).isEqualTo(user)
    }

    @Test
    fun `should update the recent user activity correctly`() = runTest {
        every { timeHelper.tomorrowInMillis() } returns 10
        val updatedActivity = recentUserActivityLocalSourceImpl.updateRecentUserActivity {
            it.apply {
                it.enrolmentsToday = 10
            }
        }
        assertThat(updatedActivity.enrolmentsToday).isEqualTo(10)

        val recentActivity = recentUserActivityLocalSourceImpl.getRecentUserActivity()
        assertThat(recentActivity.enrolmentsToday).isEqualTo(10)
    }

    @Test
    fun `should clear the recent user activity correctly`() = runTest {
        recentUserActivityLocalSourceImpl.updateRecentUserActivity {
            it.apply {
                it.enrolmentsToday = 10
            }
        }
        recentUserActivityLocalSourceImpl.clearRecentActivity()

        val recentActivity = recentUserActivityLocalSourceImpl.getRecentUserActivity()
        assertThat(recentActivity.enrolmentsToday).isEqualTo(0)
    }
}
