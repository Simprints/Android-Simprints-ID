package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.PeopleCount
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTaskImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CountTaskTest {

    private val remotePeopleManagerMock: RemotePeopleManager = mock()
    private val localDbManagerMock: LocalDbManager = mock()
    private val dbManagerMock: DbManager = mock()
    private val preferencesManagerMock: PreferencesManager = mock()
    private val loginInfoManagerMock: LoginInfoManager = mock()

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .rescheduleRxMainThread()
            .setupFirebase()
    }

    @Test
    fun testCountForGlobalSync_shouldSucceed() {
        val nPeopleInRemote = 22000
        val peopleInLocalDb = 2000

        val testObserver = makeFakeNumberOfPeopleToDownSyncCountRequest(
            getMockListOfPeopleCountWithCounter(22000),
            peopleInLocalDb
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue { peopleToDownload ->
                peopleToDownload.sumBy { it.count } == nPeopleInRemote - peopleInLocalDb
            }
    }

    private fun makeFakeNumberOfPeopleToDownSyncCountRequest(peopleToDownload: List<PeopleCount>,
                                                             peopleInLocalDb: Int): TestObserver<List<PeopleCount>> {

        whenever(remotePeopleManagerMock.getDownSyncPeopleCount(anyNotNull())).thenReturn(Single.just(peopleToDownload))
        whenever(localDbManagerMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(peopleInLocalDb))
        whenever(preferencesManagerMock.syncGroup).thenReturn(GROUP.GLOBAL)
        whenever(preferencesManagerMock.selectedModules).thenReturn(setOf("0"))
        whenever(loginInfoManagerMock.getSignedInUserIdOrEmpty()).thenReturn("")
        whenever(loginInfoManagerMock.getSignedInProjectIdOrEmpty()).thenReturn("")
        whenever(dbManagerMock.getPeopleCountToDownSync(anyNotNull())).thenReturn(Single.just(getMockListOfPeopleCountWithCounter(20000)))

        val peopleToDownSyncTask = CountTaskImpl(dbManagerMock)
        return peopleToDownSyncTask.execute(SyncScope(DEFAULT_PROJECT_ID, null, null)).test()
    }

    private fun getMockListOfPeopleCountWithCounter(counter: Int) =
        listOf(PeopleCount("projectId", "userId", "0", listOf(Modes.FACE, Modes.FINGERPRINT), counter))
}
