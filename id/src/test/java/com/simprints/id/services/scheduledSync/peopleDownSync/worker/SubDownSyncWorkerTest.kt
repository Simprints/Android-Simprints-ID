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
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTask
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubDownSyncWorker
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Completable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SubDownSyncWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject lateinit var context: Context
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var crashReportManagerMock: CrashReportManager
    @Mock lateinit var workParams: WorkerParameters

    private lateinit var subDownSyncWorker: SubDownSyncWorker
    private val subSyncScope = SubSyncScope("projectId", "userId", "moduleId")

    private var mockDownSyncTask: DownSyncTask = mock()

    private val module by lazy {
        TestAppModule(app,
            crashReportManagerRule = DependencyRule.MockRule,
            localDbManagerRule = DependencyRule.MockRule,
            downSyncTaskRule = DependencyRule.ReplaceRule { mockDownSyncTask }
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        MockitoAnnotations.initMocks(this)
        whenever(mockDownSyncTask.execute(anyNotNull())).thenReturn(Completable.complete())
        subDownSyncWorker = SubDownSyncWorker(context, workParams)
    }

    @Test
    fun executeWorkerWithCount_shouldSucceed() {
        whenever(workParams.inputData).thenReturn(workDataOf(
            SubDownSyncWorker.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(subSyncScope),
            subSyncScope.uniqueKey to intArrayOf(5)))
        val result = subDownSyncWorker.doWork()

        verifyOnce(mockDownSyncTask) { execute(anyNotNull()) }
        assert(result is ListenableWorker.Result.Success)
    }

    @Test
    fun tasksFails_shouldFail() {
        whenever(workParams.inputData).thenReturn(workDataOf(
            SubDownSyncWorker.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(subSyncScope),
            subSyncScope.uniqueKey to intArrayOf(-1)))

        whenever(mockDownSyncTask.execute(anyNotNull())).thenReturn(Completable.error(Throwable("some_error")))
        val result = subDownSyncWorker.doWork()

        verifyOnce(mockDownSyncTask) { execute(anyNotNull()) }
        verifyOnce(crashReportManagerMock) { logExceptionOrThrowable(anyNotNull()) }
        assert(result is ListenableWorker.Result.Failure)
    }
}
