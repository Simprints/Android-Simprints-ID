package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTask
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SubDownSyncWorker
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.workManager.initWorkManagerIfRequired
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.Completable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SubDownSyncWorkerTest: DaggerForTests() {

    @Inject lateinit var context: Context
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var analyticsManagerMock: AnalyticsManager
    @Mock lateinit var workParams: WorkerParameters

    private lateinit var subDownSyncWorker: SubDownSyncWorker
    private val subSyncScope = SubSyncScope("projectId", "userId", "moduleId")

    var mockDownSyncTask:DownSyncTask = mock()

    override var module: AppModuleForTests by lazyVar {
        object: AppModuleForTests(app) {
            override fun provideAnalyticsManager(loginInfoManager: LoginInfoManager, preferencesManager: PreferencesManager, firebaseAnalytics: FirebaseAnalytics): AnalyticsManager {
                return mock()
            }

            override fun provideLocalDbManager(ctx: Context): LocalDbManager {
                return mock()
            }
            override fun provideDownSyncTask(localDbManager: LocalDbManager,
                                             remoteDbManager: RemoteDbManager,
                                             timeHelper: TimeHelper,
                                             syncStatusDatabase: SyncStatusDatabase): DownSyncTask {
                return mockDownSyncTask
            }
        }
    }

    @Before
    override fun setUp() {
        app = (ApplicationProvider.getApplicationContext() as TestApplication)
        FirebaseApp.initializeApp(app)
        initWorkManagerIfRequired(app)

        super.setUp()
        testAppComponent.inject(this)

        MockitoAnnotations.initMocks(this)
        whenever(mockDownSyncTask.execute(any())).thenReturn(Completable.complete())
        subDownSyncWorker = SubDownSyncWorker(context, workParams)
    }

    @Test
    fun executeWorkerWithCount_shouldSucceed() {
        whenever(workParams.inputData).thenReturn(workDataOf(
            SubDownSyncWorker.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(subSyncScope),
            subSyncScope.uniqueKey to intArrayOf(5)))
        val result = subDownSyncWorker.doWork()

        verify(mockDownSyncTask, times(1)).execute(anyNotNull())
        assertEquals(ListenableWorker.Result.SUCCESS, result)
    }

    @Test
    fun tasksFails_shouldFail() {
        whenever(workParams.inputData).thenReturn(workDataOf(
            SubDownSyncWorker.SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT to syncScopesBuilder.fromSubSyncScopeToJson(subSyncScope),
            subSyncScope.uniqueKey to intArrayOf(-1)))

        whenever(mockDownSyncTask.execute(any())).thenReturn(Completable.error(Throwable("some_error")))
        val result = subDownSyncWorker.doWork()

        verify(mockDownSyncTask, times(1)).execute(anyNotNull())
        verify(analyticsManagerMock, times(1)).logThrowable(any())
        assertEquals(ListenableWorker.Result.FAILURE, result)
    }
}
