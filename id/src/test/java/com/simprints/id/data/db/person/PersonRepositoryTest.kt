package com.simprints.id.data.db.person

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncDao
import com.simprints.id.data.db.syncscope.domain.DownSyncScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.testtools.common.syntax.*
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class PersonRepositoryTest {

    companion object {
        const val REMOTE_PEOPLE_FOR_SUBSYNC = 10
    }

    private val syncScopeByProject = DownSyncScope(DEFAULT_PROJECT_ID, null, null)
    private val syncScopeByUser = DownSyncScope(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null)
    private val syncScopeByModule = DownSyncScope(DEFAULT_PROJECT_ID, null, setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2))

    @Mock lateinit var remoteDataSource: PersonRemoteDataSource
    @Mock lateinit var localDataSource: PersonLocalDataSource
    @Mock lateinit var peopleUpSyncMaster: PeopleUpSyncMaster
    @Mock lateinit var downSyncDao: DownSyncDao
    private lateinit var personRepository: PersonRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        personRepository = PersonRepositoryImpl(remoteDataSource, localDataSource, peopleUpSyncMaster, downSyncDao)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByProjectShouldReturnTheRightTotal() = runBlockingTest {

        with(syncScopeByProject.toSubSyncScopes()) {
            assesDownSyncCount(associate { Pair(it, REMOTE_PEOPLE_FOR_SUBSYNC) })
        }
    }

    @Test
    fun givenRemoteCount_countToDownSyncByUserShouldReturnTheRightTotal() = runBlockingTest {

        with(syncScopeByUser.toSubSyncScopes()) {
            assesDownSyncCount(associate { Pair(it, REMOTE_PEOPLE_FOR_SUBSYNC) })
        }
    }

    @Test
    fun givenRemoteCount_countToDownSyncByModulesShouldReturnTheRightTotal() = runBlockingTest {

        with(syncScopeByModule.toSubSyncScopes()) {
            assesDownSyncCount(associate { Pair(it, REMOTE_PEOPLE_FOR_SUBSYNC) })
        }
    }

    @Test
    fun givenANewPatient_shouldBeSavedAndUploaded() = runBlockingTest {
        val person = PeopleGeneratorUtils.getRandomPerson()
        wheneverOnSuspend(localDataSource) { insertOrUpdate(anyNotNull()) } thenOnBlockingReturn Unit
        whenever(peopleUpSyncMaster) { schedule(anyNotNull()) } thenDoNothing {}

        personRepository.saveAndUpload(person)

        verifyBlockingExactly(1, localDataSource) { insertOrUpdate(listOf(person)) }
        verifyBlockingExactly(1, peopleUpSyncMaster) { schedule(person.projectId) }
    }

    @Test
    fun givenAPatientInLocal_shouldBeLoaded() = runBlockingTest {
        val person = PeopleGeneratorUtils.getRandomPerson()
        wheneverOnSuspend(localDataSource) { load(anyNotNull()) } thenOnBlockingReturn flowOf(person)


        val fetch = personRepository.loadFromRemoteIfNeeded(person.projectId, person.patientId)

        assertThat(fetch.person).isEqualTo(person)
        assertThat(fetch.personSource).isEqualTo(PersonFetchResult.PersonSource.LOCAL)
    }

    @Test
    fun givenAPatientOnlyInRemote_shouldBeLoaded() = runBlockingTest {
        val person = PeopleGeneratorUtils.getRandomPerson()
        wheneverOnSuspend(localDataSource) { load(anyNotNull()) } thenOnBlockingReturn flowOf()
        whenever(remoteDataSource) { downloadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.just(person)

        val fetch = runBlocking {
            personRepository.loadFromRemoteIfNeeded(person.projectId, person.patientId)
        }

        assertThat(fetch.person).isEqualTo(person)
        assertThat(fetch.personSource).isEqualTo(PersonFetchResult.PersonSource.REMOTE)
        fetch.person?.let {
            verifyOnce(localDataSource) { runBlocking { insertOrUpdate(listOf(it)) } }
        }
    }

    private fun assesDownSyncCount(remoteCounts: Map<SubSyncScope, Int>) {

        whenever(remoteDataSource) { getDownSyncPeopleCount(anyNotNull(), anyNotNull()) } thenReturn Single.just(remoteCounts.map {
            val subSync = it.component1()
            with(subSync) {
                PeopleCount(projectId, userId, moduleId, listOf(Modes.FINGERPRINT), it.component2())
            }
        })

        val tester = personRepository.countToDownSync(syncScopeByProject).test()
        tester.awaitAndAssertSuccess()

        assertThat(tester.values().size).isEqualTo(1)
        val countForSyncScope = tester.values().first()
        assertThat(countForSyncScope.sumBy { it.count }).isEqualTo(remoteCounts.values.sum())
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
