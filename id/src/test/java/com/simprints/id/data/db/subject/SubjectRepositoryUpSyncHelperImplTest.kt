//package com.simprints.id.data.db.subject
//
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.google.common.truth.Truth.assertThat
//import com.simprints.core.tools.EncodingUtils
//import com.simprints.id.data.db.event.domain.events.*
//import com.simprints.id.data.db.event.domain.events.subject.*
//import com.simprints.id.data.db.subject.domain.FaceSample
//import com.simprints.id.data.db.subject.domain.FingerIdentifier
//import com.simprints.id.data.db.subject.domain.FingerprintSample
//import com.simprints.id.data.db.subject.domain.Subject
//import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
//import com.simprints.id.data.db.event.remote.EventRemoteDataSource
//import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
//import com.simprints.id.data.loginInfo.LoginInfoManager
//import com.simprints.id.domain.modality.Modality
//import com.simprints.id.domain.modality.toMode
//import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
//import com.simprints.id.testtools.UnitTestConfig
//import com.simprints.testtools.common.channel.testChannel
//import com.simprints.testtools.common.syntax.assertThrows
//import io.mockk.*
//import io.mockk.impl.annotations.RelaxedMockK
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.asFlow
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.util.*

// StopShip: to fix once the event remote data source is sorted
//@RunWith(AndroidJUnit4::class)
//@ExperimentalCoroutinesApi
//class SubjectRepositoryUpSyncHelperImplTest {
//    @RelaxedMockK lateinit var loginInfoManager: LoginInfoManager
//    @RelaxedMockK lateinit var subjectLocalDataSource: SubjectLocalDataSource
//    @RelaxedMockK lateinit var eventRemoteDataSource: EventRemoteDataSource
//    @RelaxedMockK lateinit var subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository
//    private val modalities = listOf(Modality.FACE, Modality.FINGER)
//
//    private lateinit var subjectRepositoryUpSyncHelper: SubjectRepositoryUpSyncHelperImpl
//
//    private val projectIdToSync = "projectIdToSync"
//    private val userIdToSync = "userIdToSync"
//    private val batchSize = 2
//
//    private val notYetSyncedSubject1 = Subject(
//        "subjectId1", "projectId", "userId", "moduleId", Date(1), null, true,
//        listOf(FingerprintSample(FingerIdentifier.LEFT_THUMB, EncodingUtils.base64ToBytes("finger_template"), 70)),
//        listOf(FaceSample(EncodingUtils.base64ToBytes("face_template")))
//    )
//    private val notYetSyncedSubject2 = notYetSyncedSubject1.copy(subjectId = "subjectId2")
//    private val notYetSyncedSubject3 = notYetSyncedSubject1.copy(subjectId = "subjectId3")
//
//    private val syncedSubject1 = notYetSyncedSubject1.copy(toSync = false)
//    private val syncedSubject2 = notYetSyncedSubject2.copy(toSync = false)
//    private val syncedSubject3 = notYetSyncedSubject3.copy(toSync = false)
//
//    @Before
//    fun setUp() {
//        UnitTestConfig(this)
//            .coroutinesMainThread()
//        MockKAnnotations.init(this)
//
//        subjectRepositoryUpSyncHelper = spyk(SubjectRepositoryUpSyncHelperImpl(loginInfoManager,
//            subjectLocalDataSource, eventRemoteDataSource, subjectsUpSyncScopeRepository, modalities))
//        setupBatchSize()
//        mockHelperToGenerateSameUuidForEvents()
//    }
//
//    @Test
//    fun userNotSignedIn1_shouldThrowIllegalStateException() {
//        runBlocking {
//            val expectedExceptionMessage = "People can only be uploaded when signed in"
//
//            val exceptionMessage = assertThrows<IllegalStateException> {
//                withContext(Dispatchers.IO) {
//                    subjectRepositoryUpSyncHelper.executeUploadWithProgress(this)
//                }
//            }.message
//            assertThat(exceptionMessage).isEqualTo(expectedExceptionMessage)
//        }
//    }
//
//    @Test
//    fun simprintsInternalServerException_shouldWrapInSyncCloudIntegrationException() {
//        runBlocking {
//            mockSignedInUser(projectIdToSync, userIdToSync)
//            mockSuccessfulLocalSubjectsQueries(listOf(notYetSyncedSubject1))
//            coEvery { eventRemoteDataSource.post(projectIdToSync, any()) } throws SyncCloudIntegrationException("", Throwable())
//
//            assertThrows<SyncCloudIntegrationException> {
//                withContext(Dispatchers.IO) {
//                    subjectRepositoryUpSyncHelper.executeUploadWithProgress(this)
//                }
//            }
//        }
//    }
//
//    @Test
//    fun singleBatchOfSubjects_uploadIt_shouldSucceed() {
//        val subjectsBatches = arrayOf(listOf(notYetSyncedSubject1, notYetSyncedSubject2))
//        val events = createEventsFromSubjects(
//            arrayOf(
//                listOf(notYetSyncedSubject1, notYetSyncedSubject2)
//            )
//        )
//
//        testSuccessfulUpSync(
//            localQueryResults = subjectsBatches,
//            expectedUploadBatches = events,
//            expectedLocalUpdates = arrayOf(listOf(syncedSubject1, syncedSubject2))
//        )
//    }
//
//    @Test
//    fun moreThanBatchSizeFromLocal_uploadIt_shouldSucceedByCreatingBatches() {
//        val subjectsBatches = arrayOf(listOf(notYetSyncedSubject1, notYetSyncedSubject2, notYetSyncedSubject3))
//        val events = createEventsFromSubjects(
//            arrayOf(
//                listOf(notYetSyncedSubject1, notYetSyncedSubject2),
//                listOf(notYetSyncedSubject3)
//            )
//        )
//
//        testSuccessfulUpSync(
//            localQueryResults = subjectsBatches,
//            expectedUploadBatches = events,
//            expectedLocalUpdates = arrayOf(
//                listOf(syncedSubject1, syncedSubject2),
//                listOf(syncedSubject3)
//            )
//        )
//    }
//
//    @Test
//    fun multipleBatchesFromLocal_uploadIt_shouldSucceed() {
//        val subjectsBatches = arrayOf(
//            listOf(notYetSyncedSubject1, notYetSyncedSubject2),
//            listOf(notYetSyncedSubject3)
//        )
//        val events = createEventsFromSubjects(
//            arrayOf(
//                listOf(notYetSyncedSubject1, notYetSyncedSubject2),
//                listOf(notYetSyncedSubject3)
//            )
//        )
//
//        testSuccessfulUpSync(
//            localQueryResults = subjectsBatches,
//            expectedUploadBatches = events,
//            expectedLocalUpdates = arrayOf(
//                listOf(syncedSubject1, syncedSubject2),
//                listOf(syncedSubject3)
//            )
//        )
//    }
//
//    private fun setupBatchSize() {
//        every { subjectRepositoryUpSyncHelper.batchSize } returns batchSize
//    }
//
//    private fun mockHelperToGenerateSameUuidForEvents() {
//        every { subjectRepositoryUpSyncHelper.getRandomUuid() } returns "random_uuid"
//    }
//
//    private fun testSuccessfulUpSync(
//        localQueryResults: Array<List<Subject>>,
//        expectedUploadBatches: Array<Events>,
//        expectedLocalUpdates: Array<List<Subject>>
//    ) {
//        runBlocking {
//
//            mockSignedInUser(projectIdToSync, userIdToSync)
//            mockSuccessfulLocalSubjectsQueries(*localQueryResults)
//
//            withContext(Dispatchers.IO) {
//                subjectRepositoryUpSyncHelper.executeUploadWithProgress(this).testChannel()
//                verifyLocalSubjectsQueries()
//                verifySubjectsUploads(expectedUploadBatches)
//                verifyLocalSubjectsUpdates(*expectedLocalUpdates)
//            }
//        }
//
//    }
//
//    private fun createEventsFromSubjects(subjects: Array<List<Subject>>) =
//        subjects.map {
//            Events(it.map { createEventFromSubject(it) })
//        }.toTypedArray()
//
//    private fun createEventFromSubject(subject: Subject): Event =
//        with(subject) {
//            Event(
//                    "random_uuid",
//                    listOf(projectId),
//                    listOf(subjectId),
//                    listOf(attendantId),
//                    listOf(moduleId),
//                    modalities.map { it.toMode() },
//                    createPayload(subject)
//            )
//        }
//
//    private fun createPayload(subject: Subject) =
//        EnrolmentRecordCreationPayload(
//            subjectId = subject.subjectId,
//            projectId = subject.projectId,
//            moduleId = subject.moduleId,
//            attendantId = subject.attendantId,
//            biometricReferences = buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
//        )
//
//    private fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>, faceSamples: List<FaceSample>) =
//        listOf(
//            FingerprintReference(
//                fingerprintSamples.map {
//                    FingerprintTemplate(it.templateQualityScore,
//                        EncodingUtils.byteArrayToBase64(it.template),
//                        it.fingerIdentifier.fromSubjectToEvent())
//                }),
//            FaceReference(
//                faceSamples.map {
//                    FaceTemplate(
//                        EncodingUtils.byteArrayToBase64(it.template)
//                    )
//                })
//        )
//
//    private fun mockSignedInUser(projectId: String, userId: String) {
//        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns projectId
//        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns userId
//    }
//
//    private fun mockSuccessfulLocalSubjectsQueries(vararg queryResults: List<Subject>) {
//        coEvery { subjectLocalDataSource.load(any()) } coAnswers {
//            queryResults.fold(emptyList<Subject>()) { aggr, new -> aggr + new }.toList().asFlow()
//        }
//    }
//
//    private fun verifyLocalSubjectsQueries() {
//        coVerify(exactly = 1) {
//            subjectLocalDataSource.load(withArg {
//                assertThat(it.toSync).isEqualTo(true)
//            })
//        }
//    }
//
//    private fun verifySubjectsUploads(events: Array<Events>) {
//        events.forEach {
//            coVerify(exactly = 1) { eventRemoteDataSource.post(projectIdToSync, it) }
//        }
//    }
//
//    private fun verifyLocalSubjectsUpdates(vararg updates: List<Subject>) {
//        updates.forEach { update ->
//            coVerify(exactly = 1) { subjectLocalDataSource.insertOrUpdate(update) }
//        }
//    }
//}
