package com.simprints.id.data.db.person

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncDao
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
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
        const val REMOTE_PEOPLE_FOR_SUBSYNC_CREATE = 100
        const val REMOTE_PEOPLE_FOR_SUBSYNC_DELETE = 10
        const val REMOTE_PEOPLE_FOR_SUBSYNC_UPDATE = 2
    }

    private val syncScopeByProject = SyncScope(DEFAULT_PROJECT_ID, null, null)
    private val syncScopeByUser = SyncScope(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null)
    private val syncScopeByModule = SyncScope(DEFAULT_PROJECT_ID, null, setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2))

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
        assessPeopleOperationsCount(syncScopeByProject)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByUserShouldReturnTheRightTotal() = runBlockingTest {
        assessPeopleOperationsCount(syncScopeByUser)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByModulesShouldReturnTheRightTotal() = runBlockingTest {
        assessPeopleOperationsCount(syncScopeByModule)
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

    private fun assessPeopleOperationsCount(syncScope: SyncScope) {
        whenever(remoteDataSource) { getDownSyncPeopleCount(anyNotNull(), anyNotNull()) } thenReturn Single.just(
            syncScope.toSubSyncScopes().map {
                PeopleCount(it.projectId, it.userId, it.moduleId, listOf(Modes.FINGERPRINT),
                    REMOTE_PEOPLE_FOR_SUBSYNC_CREATE, REMOTE_PEOPLE_FOR_SUBSYNC_DELETE, REMOTE_PEOPLE_FOR_SUBSYNC_UPDATE)
            }
        )

        val tester = personRepository.countToDownSync(syncScopeByProject).test()
        tester.awaitAndAssertSuccess()

        assertThat(tester.values().size).isEqualTo(1)
        val countForSyncScope = tester.values().first()

        with(countForSyncScope) {
            assertThat(sumBy { it.downloadCount }).isEqualTo(REMOTE_PEOPLE_FOR_SUBSYNC_CREATE * size)
            assertThat(sumBy { it.deleteCount }).isEqualTo(REMOTE_PEOPLE_FOR_SUBSYNC_DELETE * size)
            assertThat(sumBy { it.updateCount }).isEqualTo(REMOTE_PEOPLE_FOR_SUBSYNC_UPDATE * size)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
