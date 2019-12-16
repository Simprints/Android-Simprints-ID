package com.simprints.id.data.db.person

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.down_sync_info.domain.*
import com.simprints.id.data.db.people_sync.down.domain.*
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.sync.peopleUpsync.PeopleUpSyncMaster
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

    private val modes = listOf(Modes.FACE, Modes.FINGERPRINT)
    private val syncScopeByProject = ProjectSyncScope(DEFAULT_PROJECT_ID, modes)
    private val syncScopeByUser = UserSyncScope(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, modes)
    private val syncScopeByModule = ModuleSyncScope(DEFAULT_PROJECT_ID, listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2), modes)

    @Mock lateinit var remoteDataSource: PersonRemoteDataSource
    @Mock lateinit var localDataSource: PersonLocalDataSource
    @Mock lateinit var peopleUpSyncMaster: PeopleUpSyncMaster
    @Mock lateinit var downSyncScopeRepository: PeopleDownSyncScopeRepository

    private lateinit var personRepository: PersonRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        personRepository = PersonRepositoryImpl(remoteDataSource, localDataSource, downSyncScopeRepository, peopleUpSyncMaster)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByProjectShouldReturnTheRightTotal() = runBlockingTest {
        assesDownSyncCount(syncScopeByProject)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByUserShouldReturnTheRightTotal() = runBlockingTest {
        assesDownSyncCount(syncScopeByProject)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByModulesShouldReturnTheRightTotal() = runBlockingTest {
        assesDownSyncCount(syncScopeByProject)
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

    private suspend fun assesDownSyncCount(downSyncScope: PeopleDownSyncScope) {
        val ops = downSyncScopeRepository.getDownSyncOperations(downSyncScope)
        whenever(remoteDataSource) { getDownSyncPeopleCount(anyNotNull(), anyNotNull()) } thenReturn Single.just(ops.map {
            PeopleCount(REMOTE_PEOPLE_FOR_SUBSYNC, 0, 0)
        })

        val counts = personRepository.countToDownSync(downSyncScope)

        assertThat(counts.size).isEqualTo(ops.size)
        assertThat(counts.map { it.totalCount() }).isEqualTo(REMOTE_PEOPLE_FOR_SUBSYNC * ops.size)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
