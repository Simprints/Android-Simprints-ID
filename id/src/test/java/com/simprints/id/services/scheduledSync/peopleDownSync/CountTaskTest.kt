package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.GROUP.*
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
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

    private val personRemoteDataSourceMock: PersonRemoteDataSource = mock()
    private val personRepositoryMock: PersonRepository = mock()
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

        val testObserver = makeFakeNumberOfPeopleToDownSyncCountRequest(
            getMockListOfPeopleCountWithCounter(nPeopleInRemote), GLOBAL
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue { peopleToDownload ->
                peopleToDownload.sumBy { it.count } == nPeopleInRemote
            }
    }

    @Test
    fun testCountForUserSync_shouldSucceed() {
        val nPeopleInRemote = 2000

        val testObserver = makeFakeNumberOfPeopleToDownSyncCountRequest(
            getMockListOfPeopleCountWithCounter(nPeopleInRemote), USER
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue { peopleToDownload ->
                peopleToDownload.sumBy { it.count } == nPeopleInRemote
            }
    }

    @Test
    fun testCountForModuleSync_shouldSucceed() {
        val nPeopleInRemote = 3000

        val testObserver = makeFakeNumberOfPeopleToDownSyncCountRequest(
            getMockListOfPeopleCountForModulesWithCounter(), MODULE
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue { peopleToDownload ->
                peopleToDownload.sumBy { it.count } == nPeopleInRemote
            }
    }

    private fun makeFakeNumberOfPeopleToDownSyncCountRequest(peopleToDownload: List<PeopleCount>,
                                                             syncGroup: GROUP): TestObserver<List<PeopleCount>> {

        whenever(personRemoteDataSourceMock.getDownSyncPeopleCount(anyNotNull(), anyNotNull())).thenReturn(Single.just(peopleToDownload))
        whenever(preferencesManagerMock.syncGroup).thenReturn(syncGroup)
        whenever(preferencesManagerMock.selectedModules).thenReturn(setOf("0", "1", "2"))
        whenever(loginInfoManagerMock.getSignedInUserIdOrEmpty()).thenReturn("")
        whenever(loginInfoManagerMock.getSignedInProjectIdOrEmpty()).thenReturn("")
        whenever(personRepositoryMock) { countToDownSync(anyNotNull()) } thenReturn Single.just(getMockListOfPeopleCountWithCounter(peopleToDownload.sumBy { it.count }))

        val peopleToDownSyncTask = CountTaskImpl(personRepositoryMock)
        return peopleToDownSyncTask.execute(SyncScope(DEFAULT_PROJECT_ID, null, null)).test()
    }

    private fun getMockListOfPeopleCountWithCounter(counter: Int) =
        listOf(PeopleCount("projectId", "userId", "0", listOf(Modes.FACE, Modes.FINGERPRINT), counter))

    private fun getMockListOfPeopleCountForModulesWithCounter() =
        listOf(
            PeopleCount("projectId", "userId", "0", listOf(Modes.FACE, Modes.FINGERPRINT), 1000),
            PeopleCount("projectId", "userId", "1", listOf(Modes.FACE, Modes.FINGERPRINT), 1000),
            PeopleCount("projectId", "userId", "2", listOf(Modes.FACE, Modes.FINGERPRINT), 1000)
        )

}
