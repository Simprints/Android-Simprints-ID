package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.work.*
import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTask
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubDownSyncWorker
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.Completable
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import timber.log.Timber
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SubDownSyncWorkerTest: DaggerForTests() {

    @Inject lateinit var context: Context
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var downSyncTask: DownSyncTask
    @Inject lateinit var analyticsManager: AnalyticsManager

    @Mock lateinit var workParams: WorkerParameters
    private lateinit var subDownSyncWorker: SubDownSyncWorker
    private val subSyncScope = SubSyncScope("projectId", "userId", "moduleId")

    override var module by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = DependencyRule.MockRule,
            countTaskRule = DependencyRule.MockRule,
            analyticsManagerRule = DependencyRule.SpyRule,
            downSyncTaskRule = DependencyRule.MockRule)
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        try {
            WorkManager.initialize(app, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Timber.d("WorkManager already initialized")
        }
        super.setUp()
        testAppComponent.inject(this)
        MockitoAnnotations.initMocks(this)
        subDownSyncWorker = SubDownSyncWorker(context, workParams)
    }

    @Test
    fun executeWorkerWithCount_shouldSucceed() {
        whenever(workParams.inputData).thenReturn(workDataOf(
            SubDownSyncWorker.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(subSyncScope),
            subSyncScope.uniqueKey to intArrayOf(5)))
        whenever(downSyncTask.execute(anyNotNull())).thenReturn(Completable.complete())
        val result = subDownSyncWorker.doWork()

        verify(downSyncTask, times(1)).execute(anyNotNull())
        assertEquals(ListenableWorker.Result.SUCCESS, result)
    }

    @Test
    fun executeWorkerWithZeroCount_shouldSucceed() {
        whenever(workParams.inputData).thenReturn(workDataOf(
            SubDownSyncWorker.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(subSyncScope),
            subSyncScope.uniqueKey to intArrayOf(0)))
        whenever(downSyncTask.execute(anyNotNull())).thenReturn(Completable.complete())
        val result = subDownSyncWorker.doWork()

        verify(downSyncTask, times(0)).execute(anyNotNull())
        assertEquals(ListenableWorker.Result.SUCCESS, result)
    }

    @Test
    fun executeWorkerWithInvalidValue_shouldFail() {
        whenever(workParams.inputData).thenReturn(workDataOf(
            SubDownSyncWorker.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(subSyncScope),
            subSyncScope.uniqueKey to intArrayOf(-1)))
        whenever(downSyncTask.execute(anyNotNull())).thenReturn(Completable.complete())
        val result = subDownSyncWorker.doWork()

        verify(downSyncTask, times(0)).execute(anyNotNull())
        verify(analyticsManager, times(1)).logThrowable(any())
        assertEquals(ListenableWorker.Result.FAILURE, result)
    }


}
