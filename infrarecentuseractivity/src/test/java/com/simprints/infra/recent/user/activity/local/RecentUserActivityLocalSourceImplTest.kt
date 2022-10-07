package com.simprints.infra.recent.user.activity.local

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
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
        produceFile = { testContext.dataStoreFile(TEST_DATASTORE_NAME) }
    )
    private val recentUserActivityLocalSourceImpl = RecentUserActivityLocalSourceImpl(testDataStore)

    @Test
    fun `should update the recent user activity correctly`() = runTest {
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
