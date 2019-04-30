package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.domain.PeopleCount
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.CountWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.CountWorker.Companion.COUNT_WORKER_SCOPE_INPUT
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CountWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject lateinit var context: Context
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var crashReportManager: CrashReportManager

    @Mock lateinit var workParams: WorkerParameters

    private val countTaskMock: CountTask = mock()

    private lateinit var countWorker: CountWorker
    private val subSyncScope = SubSyncScope("projectId", "userId", "moduleId")

    private val module by lazy {
        TestAppModule(app,
            localDbManagerRule = DependencyRule.MockRule,
            crashReportManagerRule = DependencyRule.MockRule,
            countTaskRule = DependencyRule.ReplaceRule { countTaskMock }
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        MockitoAnnotations.initMocks(this)
        countWorker = CountWorker(context, workParams)
        whenever(workParams.inputData).thenReturn(
            workDataOf(
                COUNT_WORKER_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(
                    subSyncScope
                )
            )
        )
    }

    @Test
    fun testWorkerSuccessAndOutputData_shouldSucceedWithCorrectData() {
        whenever(countTaskMock.execute(anyNotNull())).thenReturn(Single.just(getMockListOfPeopleCountWithCounter(5)))
        val workerResult = countWorker.doWork()

        assert(
            workerResult is ListenableWorker.Result.Success &&
                workerResult.outputData.getInt(subSyncScope.uniqueKey, 0) == 5
        )
    }

    @Test
    fun testWorkerWithCountFailure_shouldLogErrorAndSucceed() {
        whenever(countTaskMock.execute(anyNotNull())).thenReturn(null)
        val workerResult = countWorker.doWork()

        verifyOnce(crashReportManager) { logExceptionOrThrowable(anyNotNull()) }
        assert(workerResult is ListenableWorker.Result.Success)
    }

    private fun getMockListOfPeopleCountWithCounter(counter: Int) =
        listOf(PeopleCount("projectId", "userId", "moduleId", listOf("FACE", "FINGERPRINT"), counter))
}
