package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForUnitTests
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubCountWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubCountWorker.Companion.SUBCOUNT_WORKER_SUB_SCOPE_INPUT
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.testframework.unit.RobolectricDaggerTestConfig
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
class SubCountWorkerTest : DaggerForUnitTests() {

    @Inject lateinit var context: Context
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var analyticsManager: AnalyticsManager

    @Mock lateinit var workParams: WorkerParameters

    private val countTaskMock: CountTask = mock()

    private lateinit var subCountWorker: SubCountWorker
    private val subSyncScope = SubSyncScope("projectId", "userId", "moduleId")

    override var module: AppModuleForTests by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = DependencyRule.MockRule,
            analyticsManagerRule = DependencyRule.SpyRule,
            countTaskRule = DependencyRule.ReplaceRule { countTaskMock }
        )
    }

    @Before
    fun setUp() {
        RobolectricDaggerTestConfig(this).setupAllAndFinish()

        MockitoAnnotations.initMocks(this)
        subCountWorker = SubCountWorker(context, workParams)
        whenever(workParams.inputData).thenReturn(
            workDataOf(
                SUBCOUNT_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(
                    subSyncScope
                )
            )
        )
    }

    @Test
    fun testWorkerSuccessAndOutputData_shouldSucceedWithCorrectData() {
        whenever(countTaskMock.execute(anyNotNull())).thenReturn(Single.just(5))
        val workerResult = subCountWorker.doWork()

        assert(
            workerResult is ListenableWorker.Result.Success &&
                workerResult.outputData.getInt(subSyncScope.uniqueKey, 0) == 5
        )
    }

    @Test
    fun testWorkerWithCountFailure_shouldLogErrorAndSucceed() {
        whenever(countTaskMock.execute(anyNotNull())).thenReturn(null)
        val workerResult = subCountWorker.doWork()

        verify(analyticsManager, times(1)).logThrowable(any())
        assert(workerResult is ListenableWorker.Result.Success)
    }
}
