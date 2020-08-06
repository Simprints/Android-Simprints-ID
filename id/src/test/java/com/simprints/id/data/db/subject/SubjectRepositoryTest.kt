//package com.simprints.id.data.db.subject
//
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.google.common.truth.Truth.assertThat
//import com.simprints.core.tools.EncodingUtils
//import com.simprints.core.tools.utils.randomUUID
//import com.simprints.id.commontesttools.DefaultTestConstants.moduleSyncScope
//import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
//import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
//import com.simprints.id.commontesttools.SubjectsGeneratorUtils
//import com.simprints.id.data.db.SubjectFetchResult
//import com.simprints.id.data.db.event.domain.EventCount
//import com.simprints.id.data.db.event.domain.events.*
//import com.simprints.id.data.db.subject.domain.FaceSample
//import com.simprints.id.data.db.subject.domain.FingerprintSample
//import com.simprints.id.data.db.subject.domain.Subject
//import com.simprints.id.data.db.event.domain.events.EventPayloadType.ENROLMENT_RECORD_CREATION
//import com.simprints.id.data.db.event.domain.events.subject.*
//import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
//import com.simprints.id.data.db.event.remote.EventRemoteDataSource
//import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
//import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncScope
//import com.simprints.id.services.scheduledSync.subjects.up.controllers.SubjectsUpSyncExecutor
//import com.simprints.id.testtools.UnitTestConfig
//import com.simprints.id.tools.json.SimJsonHelper
//import io.mockk.*
//import io.mockk.impl.annotations.RelaxedMockK
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.test.runBlockingTest
//import okhttp3.ResponseBody.Companion.toResponseBody
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.koin.core.context.stopKoin
//import java.io.InputStream

// StopShip: to fix once the event remote data source is sorted
//@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class)
//class SubjectRepositoryTest {
//
//    companion object {
//        const val REMOTE_SUBJECTS_FOR_SUBSYNC = 100
//    }
//
//    @RelaxedMockK lateinit var localDataSource: SubjectLocalDataSource
//    @RelaxedMockK lateinit var subjectsUpSyncExecutor: SubjectsUpSyncExecutor
//    @RelaxedMockK lateinit var downSyncScopeRepository: SubjectsDownSyncScopeRepository
//    @RelaxedMockK lateinit var eventRemoteDataSource: EventRemoteDataSource
//    @RelaxedMockK lateinit var subjectRepositoryUpSyncHelper: SubjectRepositoryUpSyncHelper
//    @RelaxedMockK lateinit var subjectRepositoryDownSyncHelper: SubjectRepositoryDownSyncHelper
//
//    private lateinit var subjectRepository: SubjectRepository
//
//    @Before
//    fun setup() {
//        UnitTestConfig(this).coroutinesMainThread()
//        MockKAnnotations.init(this, relaxUnitFun = true)
//        subjectRepository = SubjectRepositoryImpl(eventRemoteDataSource,
//            localDataSource, downSyncScopeRepository, subjectsUpSyncExecutor,
//            subjectRepositoryUpSyncHelper, subjectRepositoryDownSyncHelper)
//    }
//
//    @Test
//    fun givenRemoteCount_countToDownSyncByProjectShouldReturnTheRightTotal() = runBlockingTest {
//        assesDownSyncCount(projectSyncScope)
//    }
//
//    @Test
//    fun givenRemoteCount_countToDownSyncByUserShouldReturnTheRightTotal() = runBlockingTest {
//        assesDownSyncCount(userSyncScope)
//    }
//
//    @Test
//    fun givenRemoteCount_countToDownSyncByModulesShouldReturnTheRightTotal() = runBlockingTest {
//        assesDownSyncCount(moduleSyncScope)
//    }
//
//    @Test
//    fun givenANewSubject_shouldBeSavedAndUploaded() = runBlockingTest {
//        val subject = SubjectsGeneratorUtils.getRandomSubject()
//
//        subjectRepository.saveAndUpload(subject)
//
//        coVerify { localDataSource.insertOrUpdate(listOf(subject)) }
//        verify { subjectsUpSyncExecutor.sync() }
//    }
//
//    @Test
//    fun givenASubjectInLocal_shouldBeLoaded() = runBlockingTest {
//        val subject = SubjectsGeneratorUtils.getRandomSubject()
//        coEvery { localDataSource.load(any()) } returns flowOf(subject)
//
//        val fetch = subjectRepository.loadFromRemoteIfNeeded(subject.projectId, subject.subjectId)
//
//        assertThat(fetch.subject).isEqualTo(subject)
//        assertThat(fetch.subjectSource).isEqualTo(SubjectFetchResult.SubjectSource.LOCAL)
//    }
//
//    @Test
//    fun givenASubjectOnlyInRemote_shouldBeLoaded() {
//        runBlocking {
//            val subject = SubjectsGeneratorUtils.getRandomSubject()
//            coEvery { localDataSource.load(any()) } returns flowOf()
//            coEvery { eventRemoteDataSource.getStreaming(any()) } returns buildCreationEventStreamFromSubject(subject)
//
//            val fetch = subjectRepository.loadFromRemoteIfNeeded(subject.projectId, subject.subjectId)
//
//            with(fetch.subject) {
//                assertThat(this?.subjectId).isEqualTo(subject.subjectId)
//                assertThat(this?.attendantId).isEqualTo(subject.attendantId)
//                assertThat(this?.moduleId).isEqualTo(subject.moduleId)
//                assertThat(this?.projectId).isEqualTo(subject.projectId)
//            }
//            assertThat(fetch.subjectSource).isEqualTo(SubjectFetchResult.SubjectSource.REMOTE)
//
//            fetch.subject?.let {
//                coVerify { localDataSource.insertOrUpdate(listOf(it)) }
//            }
//        }
//    }
//
//    @Test
//    fun givenADeletedSubjectInRemote_shouldNotBeLoaded() {
//        runBlocking {
//            val subject = SubjectsGeneratorUtils.getRandomSubject()
//            coEvery { localDataSource.load(any()) } returns flowOf()
//            coEvery { eventRemoteDataSource.getStreaming(any()) } returns buildEventsStreamForSubjectWithLastEventAsDeletion(subject)
//
//            val fetch = subjectRepository.loadFromRemoteIfNeeded(subject.projectId, subject.subjectId)
//
//            assertThat(fetch.subject).isNull()
//            assertThat(fetch.subjectSource).isEqualTo(SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
//        }
//    }
//
//    @Test
//    fun givenAMoveEventForSubjectInRemote_shouldBeLoaded() {
//        runBlocking {
//            val subject = SubjectsGeneratorUtils.getRandomSubject()
//            coEvery { localDataSource.load(any()) } returns flowOf()
//            coEvery { eventRemoteDataSource.getStreaming(any()) } returns buildEventsStreamForSubjectWithLastEventAsMove(subject)
//
//            val fetch = subjectRepository.loadFromRemoteIfNeeded(subject.projectId, subject.subjectId)
//
//            with(fetch.subject) {
//                assertThat(this?.subjectId).isEqualTo(subject.subjectId)
//                assertThat(this?.attendantId).isEqualTo(subject.attendantId)
//                assertThat(this?.moduleId).isEqualTo(subject.moduleId)
//                assertThat(this?.projectId).isEqualTo(subject.projectId)
//            }
//            assertThat(fetch.subjectSource).isEqualTo(SubjectFetchResult.SubjectSource.REMOTE)
//
//            fetch.subject?.let {
//                coVerify { localDataSource.insertOrUpdate(listOf(it)) }
//            }
//        }
//    }
//
//
//    private suspend fun assesDownSyncCount(downSyncScope: SubjectsDownSyncScope) {
//        val eventCounts = listOf(EventCount(ENROLMENT_RECORD_CREATION, REMOTE_SUBJECTS_FOR_SUBSYNC))
//
//        coEvery { downSyncScopeRepository.getDownSyncOperations(any()) } returns listOf(mockk())
//        coEvery { eventRemoteDataSource.count(any()) } returns eventCounts
//
//        val counts = subjectRepository.countToDownSync(downSyncScope)
//
//        assertThat(counts.created).isEqualTo(REMOTE_SUBJECTS_FOR_SUBSYNC)
//    }
//
//    private fun buildCreationEventStreamFromSubject(subject: Subject): InputStream {
//        val event = with(subject) {
//            Event(
//                randomUUID(),
//                listOf(projectId),
//                listOf(subjectId),
//                listOf(attendantId),
//                listOf(moduleId),
//                listOf(),
//                buildCreationPayload(this)
//            ).fromDomainToApi()
//        }
//        val responseString = SimJsonHelper.gson.toJson(listOf(event))
//        return responseString.toResponseBody().byteStream()
//    }
//
//    private fun buildEventsStreamForSubjectWithLastEventAsDeletion(subject: Subject): InputStream {
//        val creationEvent = with(subject) {
//            Event(
//                randomUUID(),
//                listOf(projectId),
//                listOf(subjectId),
//                listOf(attendantId),
//                listOf(moduleId),
//                listOf(),
//                buildCreationPayload(this)
//            ).fromDomainToApi()
//        }
//
//        val deletionEvent = with(subject) {
//            Event(
//                randomUUID(),
//                listOf(projectId),
//                listOf(subjectId),
//                listOf(attendantId),
//                listOf(moduleId),
//                listOf(),
//                buildDeletionPayload(this)
//            ).fromDomainToApi()
//        }
//
//        val responseString = SimJsonHelper.gson.toJson(listOf(creationEvent, deletionEvent))
//        return responseString.toResponseBody().byteStream()
//    }
//
//    private fun buildEventsStreamForSubjectWithLastEventAsMove(subject: Subject): InputStream {
//
//        val creationEvent = with(subject) {
//            Event(
//                randomUUID(),
//                listOf(projectId),
//                listOf(subjectId),
//                listOf(attendantId),
//                listOf(moduleId),
//                listOf(),
//                buildCreationPayload(this)
//            ).fromDomainToApi()
//        }
//
//        val moveEvent = with(subject) {
//            Event(
//                randomUUID(),
//                listOf(projectId),
//                listOf(subjectId),
//                listOf(attendantId),
//                listOf(moduleId),
//                listOf(),
//                buildMovePayload(this)
//            ).fromDomainToApi()
//        }
//
//        val responseString = SimJsonHelper.gson.toJson(listOf(creationEvent, moveEvent))
//        return responseString.toResponseBody().byteStream()
//    }
//
//    private fun buildCreationPayload(subject: Subject) = with(subject) {
//        EnrolmentRecordCreationPayload(
//            subjectId,
//            projectId,
//            moduleId,
//            attendantId,
//            buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
//        )
//    }
//
//    private fun buildDeletionPayload(subject: Subject) = with(subject) {
//        EnrolmentRecordDeletionPayload(
//            subjectId,
//            projectId,
//            moduleId,
//            attendantId
//        )
//    }
//
//    private fun buildMovePayload(subject: Subject) = EnrolmentRecordMovePayload(
//        buildCreationPayload(subject),
//        buildDeletionPayload(subject)
//    )
//
//    private fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>, faceSamples: List<FaceSample>): List<BiometricReference> {
//        val fingerprintReference = FingerprintReference(fingerprintSamples.map {
//            FingerprintTemplate(it.templateQualityScore, EncodingUtils.byteArrayToBase64(it.template),
//                FingerIdentifier.valueOf(it.fingerIdentifier.name))
//        })
//        val faceReference = FaceReference(faceSamples.map { FaceTemplate(EncodingUtils.byteArrayToBase64(it.template)) })
//
//        return listOf(fingerprintReference, faceReference)
//    }
//
//
//    @After
//    fun tearDown() {
//        stopKoin()
//    }
//}
