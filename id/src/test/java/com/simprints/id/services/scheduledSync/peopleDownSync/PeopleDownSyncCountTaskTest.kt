package com.simprints.id.services.scheduledSync.peopleDownSync

import com.google.firebase.FirebaseApp
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Constants
import com.simprints.id.services.scheduledSync.peopleDownSync.peopleCount.PeopleDownSyncCountTask
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncCountTaskTest: RxJavaTest {

    private val remoteDbManagerMock: RemoteDbManager = mock()
    private val localDbManagerMock: LocalDbManager = mock()
    private val dbManagerMock: DbManager = mock()
    private val preferencesManagerMock: PreferencesManager = mock()
    private val loginInfoManagerMock: LoginInfoManager = mock()

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
    }

    @Test
    fun testCountForGlobalSync_shouldSucceed() {
        val nPeopleInRemote = 22000
        val peopleInLocalDb = 2000

        val testObserver = makeFakeNumberOfPeopleToDownSyncCountRequest(
            nPeopleInRemote,
            peopleInLocalDb
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue { peopleToDownload ->
                peopleToDownload == nPeopleInRemote - peopleInLocalDb }
    }

    private fun makeFakeNumberOfPeopleToDownSyncCountRequest(peopleToDownload: Int,
                                                             peopleInLocalDb: Int): TestObserver<Int> {

        whenever(remoteDbManagerMock.getNumberOfPatientsForSyncParams(anyNotNull())).thenReturn(Single.just(peopleToDownload))
        whenever(localDbManagerMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(peopleInLocalDb))
        whenever(preferencesManagerMock.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
        whenever(preferencesManagerMock.moduleId).thenReturn("0")
        whenever(loginInfoManagerMock.getSignedInUserIdOrEmpty()).thenReturn("")
        whenever(loginInfoManagerMock.getSignedInProjectIdOrEmpty()).thenReturn("")
        whenever(dbManagerMock.calculateNPatientsToDownSyncForSyncParams(anyInt(), anyNotNull())).thenReturn(Single.just(20000))

        val peopleToDownSyncTask = PeopleDownSyncCountTask(remoteDbManagerMock, dbManagerMock, preferencesManagerMock, loginInfoManagerMock)
        return peopleToDownSyncTask.execute().test()
    }
}
