package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.CountWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.CountWorker.Companion.COUNT_WORKER_SCOPE_INPUT
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
            crashReportManagerRule = DependencyRule.MockRule,
            countTaskRule = DependencyRule.ReplaceRule { countTaskMock }
        )
    }

    private val dataModule by lazy {
        TestDataModule(projectLocalDataSourceRule = DependencyRule.MockRule)
    }


    @Before
    fun setUp() {
        UnitTestConfig(this, module, dataModule = dataModule).fullSetup()

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
        val nPeopleToDownload = 50
        val nPeopleToDelete = 5
        val nPeopleToUpdate = 2
        whenever(countTaskMock.execute(anyNotNull())).thenReturn(Single.just(getMockListOfPeopleCountWithCounter(nPeopleToDownload, nPeopleToDelete, nPeopleToUpdate)))

        val workerResult = countWorker.doWork()

        assert(
            workerResult is ListenableWorker.Result.Success &&
                workerResult.outputData.getInt(subSyncScope.uniqueKey, 0) == nPeopleToDownload
        )
    }

    @Test
    fun testWorkerWithCountFailure_shouldLogErrorAndSucceed() {
        whenever(countTaskMock.execute(anyNotNull())).thenReturn(null)
        val workerResult = countWorker.doWork()

        verifyOnce(crashReportManager) { logExceptionOrSafeException(anyNotNull()) }
        assert(workerResult is ListenableWorker.Result.Success)
    }

    private fun getMockListOfPeopleCountWithCounter(nPeopleToDownload: Int, nPeopleToDelete: Int, nPeopleToUpdate: Int) =
        listOf(PeopleCount("projectId", "userId", "moduleId", listOf(Modes.FACE, Modes.FINGERPRINT),
            nPeopleToDownload, nPeopleToDelete, nPeopleToUpdate))
}
