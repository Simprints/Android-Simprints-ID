package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.work.testing.TestListenableWorkerBuilder
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.count.CountWorker
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
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
        app.component = mock()
        whenever(app.component) { this.inject(any<CountWorker>()) } thenDoNothing {}
        countWorker.downSyncScopeRepository = mock()
        countWorker.crashReportManager = mock()
    }

    @Test
    fun countWorker_shouldExtractTheDownSyncScopeFromTheRepo() = runBlockingTest {
        countWorker.downSyncScopeRepository = mock()

        countWorker.doWork()

        verifyOnce(countWorker.downSyncScopeRepository) { getDownSyncScope() }
    }

//    @Test
//    fun testWorkerWithCountFailure_shouldLogErrorAndSucceed() {
//        whenever(countTaskMock.execute(anyNotNull())).thenReturn(null)
//        val workerResult = countWorker.doWork()
//
//        verifyOnce(crashReportManager) { logExceptionOrSafeException(anyNotNull()) }
//        assert(workerResult is ListenableWorker.Result.Success)
//    }
//
//    private fun getMockListOfPeopleCountWithCounter(counter: Int) =
//        listOf(PeopleCount("projectId", "userId", "moduleId", listOf(Modes.FACE, Modes.FINGERPRINT), counter))
}
