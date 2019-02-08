package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Constants
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTaskImpl
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.testframework.common.syntax.mock
import com.simprints.testframework.common.syntax.whenever
import com.simprints.testframework.unit.reactive.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CountTaskTest : RxJavaTest {

    private val remotePeoplemanagerMock: RemotePeopleManager = mock()
    private val localDbManagerMock: LocalDbManager = mock()
    private val dbManagerMock: DbManager = mock()
    private val preferencesManagerMock: PreferencesManager = mock()
    private val loginInfoManagerMock: LoginInfoManager = mock()

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
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

        whenever(remotePeoplemanagerMock.getNumberOfPatients(anyNotNull(), any(), any())).thenReturn(Single.just(peopleToDownload))
        whenever(localDbManagerMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(peopleInLocalDb))
        whenever(preferencesManagerMock.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
        whenever(preferencesManagerMock.moduleId).thenReturn("0")
        whenever(loginInfoManagerMock.getSignedInUserIdOrEmpty()).thenReturn("")
        whenever(loginInfoManagerMock.getSignedInProjectIdOrEmpty()).thenReturn("")
        whenever(dbManagerMock.calculateNPatientsToDownSync(anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(20000))

        val peopleToDownSyncTask = CountTaskImpl(dbManagerMock)
        return peopleToDownSyncTask.execute(SubSyncScope( DEFAULT_PROJECT_ID, null, null)).test()
    }
}
