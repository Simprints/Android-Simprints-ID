package com.simprints.id.data.db.subject

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.moduleSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.subjectevents.*
import com.simprints.id.data.db.subject.domain.subjectevents.EventPayloadType.ENROLMENT_RECORD_CREATION
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.remote.EventRemoteDataSource
import com.simprints.id.data.db.subject.remote.models.subjectevents.fromDomainToApi
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncScope
import com.simprints.id.services.scheduledSync.subjects.up.controllers.SubjectsUpSyncExecutor
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.json.SimJsonHelper
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class SubjectRepositoryTest {

    companion object {
        const val REMOTE_PEOPLE_FOR_SUBSYNC = 100
    }

    @RelaxedMockK lateinit var localDataSource: SubjectLocalDataSource
    @RelaxedMockK lateinit var subjectsUpSyncExecutor: SubjectsUpSyncExecutor
    @RelaxedMockK lateinit var downSyncScopeRepository: SubjectsDownSyncScopeRepository
    @RelaxedMockK lateinit var eventRemoteDataSource: EventRemoteDataSource
    @RelaxedMockK lateinit var subjectRepositoryUpSyncHelper: SubjectRepositoryUpSyncHelper
    @RelaxedMockK lateinit var subjectRepositoryDownSyncHelper: SubjectRepositoryDownSyncHelper

    private lateinit var personRepository: SubjectRepository

    @Before
    fun setup() {
        UnitTestConfig(this).coroutinesMainThread()
        MockKAnnotations.init(this, relaxUnitFun = true)
        personRepository = SubjectRepositoryImpl(eventRemoteDataSource,
            localDataSource, downSyncScopeRepository, subjectsUpSyncExecutor,
            subjectRepositoryUpSyncHelper, subjectRepositoryDownSyncHelper)
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
        val person = SubjectsGeneratorUtils.getRandomPerson()

        personRepository.saveAndUpload(person)

        coVerify { localDataSource.insertOrUpdate(listOf(person)) }
        verify { subjectsUpSyncExecutor.sync() }
    }

    @Test
    fun givenAPatientInLocal_shouldBeLoaded() = runBlockingTest {
        val person = SubjectsGeneratorUtils.getRandomPerson()
        coEvery { localDataSource.load(any()) } returns flowOf(person)

        val fetch = personRepository.loadFromRemoteIfNeeded(person.projectId, person.subjectId)

        assertThat(fetch.subject).isEqualTo(person)
        assertThat(fetch.subjectSource).isEqualTo(SubjectFetchResult.SubjectSource.LOCAL)
    }

    @Test
    fun givenAPatientOnlyInRemote_shouldBeLoaded() {
        runBlocking {
            val person = SubjectsGeneratorUtils.getRandomPerson()
            coEvery { localDataSource.load(any()) } returns flowOf()
            coEvery { eventRemoteDataSource.getStreaming(any()) } returns buildCreationEventFromPerson(person)

            val fetch = personRepository.loadFromRemoteIfNeeded(person.projectId, person.subjectId)

            with(fetch.subject) {
                assertThat(this?.subjectId).isEqualTo(person.subjectId)
                assertThat(this?.attendantId).isEqualTo(person.attendantId)
                assertThat(this?.moduleId).isEqualTo(person.moduleId)
                assertThat(this?.projectId).isEqualTo(person.projectId)
            }
            assertThat(fetch.subjectSource).isEqualTo(SubjectFetchResult.SubjectSource.REMOTE)

            fetch.subject?.let {
                coVerify { localDataSource.insertOrUpdate(listOf(it)) }
            }
        }
    }


    private suspend fun assesDownSyncCount(downSyncScope: SubjectsDownSyncScope) {
        val eventCounts = listOf(EventCount(ENROLMENT_RECORD_CREATION, REMOTE_PEOPLE_FOR_SUBSYNC))

        coEvery { downSyncScopeRepository.getDownSyncOperations(any()) } returns listOf(mockk())
        coEvery { eventRemoteDataSource.count(any()) } returns eventCounts

        val counts = personRepository.countToDownSync(downSyncScope)

        assertThat(counts.created).isEqualTo(REMOTE_PEOPLE_FOR_SUBSYNC)
    }

    private fun buildCreationEventFromPerson(subject: Subject): InputStream {
        val event = with(subject) {
            Event(
                randomUUID(),
                listOf(projectId),
                listOf(subjectId),
                listOf(attendantId),
                listOf(moduleId),
                listOf(),
                buildCreationPayload(this)
            ).fromDomainToApi()
        }
        val responseString = SimJsonHelper.gson.toJson(listOf(event))
        return responseString.toResponseBody().byteStream()
    }

    private fun buildCreationPayload(subject: Subject) = with(subject) {
        EnrolmentRecordCreationPayload(
            subjectId,
            projectId,
            moduleId,
            attendantId,
            buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
        )
    }

    private fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>, faceSamples: List<FaceSample>): List<BiometricReference> {
        val fingerprintReference = FingerprintReference(fingerprintSamples.map {
            FingerprintTemplate(it.templateQualityScore, EncodingUtils.byteArrayToBase64(it.template),
                FingerIdentifier.valueOf(it.fingerIdentifier.name)) })
        val faceReference = FaceReference(faceSamples.map { FaceTemplate(EncodingUtils.byteArrayToBase64(it.template)) })

        return listOf(fingerprintReference, faceReference)
    }


    @After
    fun tearDown() {
        stopKoin()
    }
}
