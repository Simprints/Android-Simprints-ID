package com.simprints.id.services.scheduledSync.peopleDownSync

import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.spy
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.domain.Constants
import com.simprints.id.network.SimApiClient
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncCountTaskTest: RxJavaTest {

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
    }

    @Test
    fun testCountForGlobalSync_shouldSucceed() {
        val nPeopleToDownload = 22000
        val peopleInLocalDb = 2000

        val testObserver = makeFakeNumberOfPeopleToDownSyncCountRequest(
            nPeopleToDownload,
            peopleInLocalDb
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
    }

    private fun makeFakeNumberOfPeopleToDownSyncCountRequest(peopleToDownload: Int,
                                                             peopleInLocalDb: Int): TestObserver<Int> {

        val remoteDbManagerMock: RemoteDbManager = mock()
        val localDbManagerMock: LocalDbManager = mock()
        val dbManagerMock: DbManager = mock()
        val preferencesManagerMock: PreferencesManager = mock()
        val loginInfoManagerMock: LoginInfoManager = mock()


        whenever(remoteDbManagerMock.getNumberOfPatientsForSyncParams(anyNotNull())).thenReturn(Single.just(peopleToDownload))
        whenever(localDbManagerMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(peopleInLocalDb))
        whenever(dbManagerMock.calculateNPatientsToDownSync(anyNotNull(), anyNotNull())).thenReturn(Single.just(20000))
        whenever(preferencesManagerMock.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
        whenever(preferencesManagerMock.moduleId).thenReturn("0")
        whenever(loginInfoManagerMock.getSignedInUserIdOrEmpty()).thenReturn("")
        whenever(loginInfoManagerMock.getSignedInProjectIdOrEmpty()).thenReturn("")

        val peopleToDownSyncTask = PeopleDownSyncCountTask(remoteDbManagerMock, dbManagerMock, preferencesManagerMock, loginInfoManagerMock)
        return peopleToDownSyncTask.execute().test()
    }
}
