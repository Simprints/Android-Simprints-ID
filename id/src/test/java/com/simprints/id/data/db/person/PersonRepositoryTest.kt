package com.simprints.id.data.db.person

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.testtools.common.syntax.*
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class PersonRepositoryTest {

    companion object {
        const val LOCAL_PEOPLE_FOR_SUBSYNC = 5
        const val REMOTE_PEOPLE_FOR_SUBSYNC = 10
    }

    private val syncScopeByProject = SyncScope(DEFAULT_PROJECT_ID, null, null)
    private val syncScopeByUser = SyncScope(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null)
    private val syncScopeByModule = SyncScope(DEFAULT_PROJECT_ID, null, setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2))

    @Mock lateinit var remoteDataSource: PersonRemoteDataSource
    @Mock lateinit var localDataSource: PersonLocalDataSource
    @Mock lateinit var peopleUpSyncMaster: PeopleUpSyncMaster
    private lateinit var personRepository: PersonRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        personRepository = PersonRepositoryImpl(remoteDataSource, localDataSource, peopleUpSyncMaster)
    }

    @Test
    fun givenLocalAndRemoteCounts_countToDownSyncByProjectShouldReturnTheRightTotal() = runBlockingTest {

        with(syncScopeByProject.toSubSyncScopes()) {
            assesDownSyncCount(associate { Pair(it, LOCAL_PEOPLE_FOR_SUBSYNC) }, associate { Pair(it, REMOTE_PEOPLE_FOR_SUBSYNC) })
        }
    }

    @Test
    fun givenLocalAndRemoteCounts_countToDownSyncByUserShouldReturnTheRightTotal() = runBlockingTest {

        with(syncScopeByUser.toSubSyncScopes()) {
            assesDownSyncCount(associate { Pair(it, LOCAL_PEOPLE_FOR_SUBSYNC) }, associate { Pair(it, REMOTE_PEOPLE_FOR_SUBSYNC) })
        }
    }

    @Test
    fun givenLocalAndRemoteCounts_countToDownSyncByModulesShouldReturnTheRightTotal() = runBlockingTest {

        with(syncScopeByModule.toSubSyncScopes()) {
            assesDownSyncCount(associate { Pair(it, LOCAL_PEOPLE_FOR_SUBSYNC) }, associate { Pair(it, REMOTE_PEOPLE_FOR_SUBSYNC) })
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
        assertThat(fetch.fetchedOnline).isFalse()
    }

    @Test
    fun givenAPatientOnlyInRemote_shouldBeLoaded() = runBlockingTest {
        val person = PeopleGeneratorUtils.getRandomPerson()
        wheneverOnSuspend(localDataSource) { load(anyNotNull()) } thenOnBlockingReturn flowOf()
        whenever(remoteDataSource) { downloadPerson(anyNotNull(), anyNotNull()) } thenReturn Single.just(person)

        val fetch = personRepository.loadFromRemoteIfNeeded(person.projectId, person.patientId)

        assertThat(fetch.person).isEqualTo(person)
        assertThat(fetch.fetchedOnline).isTrue()
    }


    private fun assesDownSyncCount(localCounts: Map<SubSyncScope, Int>, remoteCounts: Map<SubSyncScope, Int>) {
        localCounts.keys.forEach {
            val query = createQuery(it)
            wheneverOnSuspend(localDataSource) { count(query) } thenOnBlockingReturn localCounts.getOrDefault(it, 0)
        }

        whenever(remoteDataSource) { getDownSyncPeopleCount(anyNotNull()) } thenReturn Single.just(remoteCounts.map {
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

    private fun createQuery(subSyncScope: SubSyncScope) =
        PersonLocalDataSource.Query(
            projectId = subSyncScope.projectId,
            userId = subSyncScope.userId,
            moduleId = subSyncScope.moduleId)


}

//interface PersonRepository : PersonLocalDataSource, PersonRemoteDataSource {
//
//    fun countToDownSync(syncScope: SyncScope): Single<List<PeopleCount>>
//    fun localCountForSyncScope(syncScope: SyncScope): Single<List<PeopleCount>>
//
//    suspend fun saveAndUpload(person: Person)
//    suspend fun loadFromRemoteIfNeeded(projectId: String, patientId: String): PersonFetchResult
//}
