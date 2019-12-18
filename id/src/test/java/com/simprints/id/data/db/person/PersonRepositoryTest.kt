package com.simprints.id.data.db.person

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.common.models.totalCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.ModuleSyncScope
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.people_sync.down.domain.ProjectSyncScope
import com.simprints.id.data.db.people_sync.down.domain.UserSyncScope
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncManager
import com.simprints.id.testtools.UnitTestConfig
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin

class PersonRepositoryTest {

    companion object {
        const val REMOTE_PEOPLE_FOR_SUBSYNC = 10
    }

    private val modes = listOf(Modes.FACE, Modes.FINGERPRINT)
    private val syncScopeByProject = ProjectSyncScope(DEFAULT_PROJECT_ID, modes)
    private val syncScopeByUser = UserSyncScope(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, modes)
    private val syncScopeByModule = ModuleSyncScope(DEFAULT_PROJECT_ID, listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2), modes)

    @RelaxedMockK lateinit var remoteDataSource: PersonRemoteDataSource
    @RelaxedMockK lateinit var localDataSource: PersonLocalDataSource
    @RelaxedMockK lateinit var peopleUpSyncManager: PeopleUpSyncManager
    @RelaxedMockK lateinit var downSyncScopeRepository: PeopleDownSyncScopeRepository

    private lateinit var personRepository: PersonRepository

    @Before
    fun setup() {
        UnitTestConfig(this).coroutinesMainThread()
        MockKAnnotations.init(this, relaxUnitFun = true)
        personRepository = PersonRepositoryImpl(remoteDataSource, localDataSource, downSyncScopeRepository, peopleUpSyncManager)
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

        personRepository.saveAndUpload(person)

        coVerify { localDataSource.insertOrUpdate(listOf(person)) }
        verify { peopleUpSyncManager.sync() }
    }

    @Test
    fun givenAPatientInLocal_shouldBeLoaded() = runBlockingTest {
        val person = PeopleGeneratorUtils.getRandomPerson()
        coEvery { localDataSource.load(any()) } returns flowOf(person)

        val fetch = personRepository.loadFromRemoteIfNeeded(person.projectId, person.patientId)

        assertThat(fetch.person).isEqualTo(person)
        assertThat(fetch.personSource).isEqualTo(PersonFetchResult.PersonSource.LOCAL)
    }

    @Test
    fun givenAPatientOnlyInRemote_shouldBeLoaded() {
        runBlocking {
            val person = PeopleGeneratorUtils.getRandomPerson()
            coEvery { localDataSource.load(any()) } returns flowOf()
            every { remoteDataSource.downloadPerson(any(), any()) } returns Single.just(person)

            val fetch = personRepository.loadFromRemoteIfNeeded(person.projectId, person.patientId)

            assertThat(fetch.person).isEqualTo(person)
            assertThat(fetch.personSource).isEqualTo(PersonFetchResult.PersonSource.REMOTE)

            fetch.person?.let {
                coVerify { localDataSource.insertOrUpdate(listOf(it)) }
            }
        }
    }
    
    private suspend fun assesDownSyncCount(downSyncScope: PeopleDownSyncScope) {
        val ops = listOf(
            PeopleCount(REMOTE_PEOPLE_FOR_SUBSYNC, 0, 0))

        coEvery { downSyncScopeRepository.getDownSyncOperations(any()) } returns emptyList()
        every { remoteDataSource.getDownSyncPeopleCount(any(), any()) } returns Single.just(ops)

        val counts = personRepository.countToDownSync(downSyncScope)

        assertThat(counts.size).isEqualTo(ops.size)
        assertThat(counts.sumBy { it.totalCount() }).isEqualTo(REMOTE_PEOPLE_FOR_SUBSYNC * ops.size)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
