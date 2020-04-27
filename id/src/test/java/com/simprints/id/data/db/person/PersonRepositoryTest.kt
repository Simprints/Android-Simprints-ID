package com.simprints.id.data.db.person

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.moduleSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import com.simprints.id.testtools.UnitTestConfig
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class PersonRepositoryTest {

    companion object {
        const val REMOTE_PEOPLE_FOR_SUBSYNC = 100
    }

    @RelaxedMockK lateinit var remoteDataSource: PersonRemoteDataSource
    @RelaxedMockK lateinit var localDataSource: PersonLocalDataSource
    @RelaxedMockK lateinit var peopleUpSyncExecutor: PeopleUpSyncExecutor
    @RelaxedMockK lateinit var downSyncScopeRepository: PeopleDownSyncScopeRepository
    @RelaxedMockK lateinit var eventRemoteDataSource: EventRemoteDataSource
    @RelaxedMockK lateinit var personRepositoryUpSyncHelper: PersonRepositoryUpSyncHelper

    private lateinit var personRepository: PersonRepository

    @Before
    fun setup() {
        UnitTestConfig(this).coroutinesMainThread()
        MockKAnnotations.init(this, relaxUnitFun = true)
        personRepository = PersonRepositoryImpl(remoteDataSource, eventRemoteDataSource,
            localDataSource, downSyncScopeRepository, peopleUpSyncExecutor, personRepositoryUpSyncHelper)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByProjectShouldReturnTheRightTotal() = runBlockingTest {
        assesDownSyncCount(projectSyncScope)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByUserShouldReturnTheRightTotal() = runBlockingTest {
        assesDownSyncCount(userSyncScope)
    }

    @Test
    fun givenRemoteCount_countToDownSyncByModulesShouldReturnTheRightTotal() = runBlockingTest {
        assesDownSyncCount(moduleSyncScope)
    }

    @Test
    fun givenANewPatient_shouldBeSavedAndUploaded() = runBlockingTest {
        val person = PeopleGeneratorUtils.getRandomPerson()

        personRepository.saveAndUpload(person)

        coVerify { localDataSource.insertOrUpdate(listOf(person)) }
        verify { peopleUpSyncExecutor.sync() }
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
            coEvery { remoteDataSource.downloadPerson(any(), any()) } returns person

            val fetch = personRepository.loadFromRemoteIfNeeded(person.projectId, person.patientId)

            assertThat(fetch.person).isEqualTo(person)
            assertThat(fetch.personSource).isEqualTo(PersonFetchResult.PersonSource.REMOTE)

            fetch.person?.let {
                coVerify { localDataSource.insertOrUpdate(listOf(it)) }
            }
        }
    }


    private suspend fun assesDownSyncCount(downSyncScope: PeopleDownSyncScope) {
        val eventCounts = listOf(EventCount(EventType.ENROLMENT_RECORD_CREATION, REMOTE_PEOPLE_FOR_SUBSYNC))

        coEvery { downSyncScopeRepository.getDownSyncOperations(any()) } returns emptyList()
        coEvery { eventRemoteDataSource.count(any()) } returns eventCounts

        val counts = personRepository.countToDownSync(downSyncScope)

        assertThat(counts.created).isEqualTo(REMOTE_PEOPLE_FOR_SUBSYNC)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
