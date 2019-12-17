package com.simprints.id.services.scheduledSync.sync.peopleDownSync.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.domain.ProjectSyncScope
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.count.CountWorker
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@SmallTest
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CountWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var countWorker: CountWorker

    @Before
    fun setUp() {
        UnitTestConfig(this).fullSetup()
        MockitoAnnotations.initMocks(this)
        countWorker = TestListenableWorkerBuilder<CountWorker>(app).build()
        app.component = mockk(relaxed = true)
        countWorker.downSyncScopeRepository = mockk(relaxed = true)
        countWorker.crashReportManager = mockk(relaxed = true)
        countWorker.personRepository = mockk(relaxed = true)
        countWorker.resultSetter = mockk(relaxed = true)
    }

    @Test
    fun countWorker_shouldExtractTheDownSyncScopeFromTheRepo() = runBlockingTest {
        countWorker.doWork()

        verify { countWorker.downSyncScopeRepository.getDownSyncScope() }
    }

    @Test
    fun countWorker_shouldExecuteTheTaskSuccessfully() = runBlockingTest {
        val counts = listOf(PeopleCount(1, 1, 1))
        coEvery { countWorker.personRepository.countToDownSync(any()) } returns counts
        coEvery { countWorker.downSyncScopeRepository.getDownSyncScope() } returns ProjectSyncScope(DEFAULT_PROJECT_ID, listOf(Modes.FINGERPRINT))

        countWorker.doWork()

        val expectedSuccessfulOutput = workDataOf(CountWorker.COUNT_PROGRESS to JsonHelper.gson.toJson(counts))
        verify { countWorker.resultSetter.success(expectedSuccessfulOutput) }
    }

    @Test
    fun countWorker_anNetworkErrorOccurs_shouldRetry() = runBlockingTest {
        coEvery { countWorker.personRepository.countToDownSync(any()) } throws Throwable("IO Error")
        coEvery { countWorker.downSyncScopeRepository.getDownSyncScope() } returns ProjectSyncScope(DEFAULT_PROJECT_ID, listOf(Modes.FINGERPRINT))

        countWorker.doWork()

        verify { countWorker.resultSetter.retry() }
    }

    @Test
    fun countWorker_anUnexpectedErrorOccurs_shouldFail() = runBlockingTest {
        coEvery { countWorker.downSyncScopeRepository.getDownSyncScope() } throws Throwable("Impossible to extract downSyncScope")

        countWorker.doWork()

        verify { countWorker.resultSetter.failure() }
    }
}
