package com.simprints.id.data.db.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.subjectevents.*
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.remote.EventRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.channel.testChannel
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class SubjectRepositoryUpSyncHelperImplTest {
    @RelaxedMockK lateinit var loginInfoManager: LoginInfoManager
    @RelaxedMockK lateinit var subjectLocalDataSource: SubjectLocalDataSource
    @RelaxedMockK lateinit var eventRemoteDataSource: EventRemoteDataSource
    @RelaxedMockK lateinit var subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository
    private val modalities = listOf(Modality.FACE, Modality.FINGER)

    private lateinit var personRepositoryUpSyncHelper: SubjectRepositoryUpSyncHelperImpl

    private val projectIdToSync = "projectIdToSync"
    private val userIdToSync = "userIdToSync"
    private val batchSize = 2

    private val notYetSyncedPerson1 = Subject(
        "patientId1", "projectId", "userId", "moduleId", Date(1), null, true
    )
    private val notYetSyncedPerson2 = notYetSyncedPerson1.copy(subjectId = "patientId2")
    private val notYetSyncedPerson3 = notYetSyncedPerson1.copy(subjectId = "patientId3")

    private val syncedPerson1 = notYetSyncedPerson1.copy(toSync = false)
    private val syncedPerson2 = notYetSyncedPerson2.copy(toSync = false)
    private val syncedPerson3 = notYetSyncedPerson3.copy(toSync = false)

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .coroutinesMainThread()
        MockKAnnotations.init(this)

        personRepositoryUpSyncHelper = spyk(SubjectRepositoryUpSyncHelperImpl(loginInfoManager,
            subjectLocalDataSource, eventRemoteDataSource, subjectsUpSyncScopeRepository, modalities))
        setupBatchSize()
        mockHelperToGenerateSameUuidForEvents()
    }

    @Test
    fun userNotSignedIn1_shouldThrowIllegalStateException() {
        runBlocking {
            val expectedExceptionMessage = "People can only be uploaded when signed in"
            
            val exceptionMessage = assertThrows<IllegalStateException> {
                withContext(Dispatchers.IO) {
                    personRepositoryUpSyncHelper.executeUploadWithProgress(this)
                }
            }.message
            assertThat(exceptionMessage).isEqualTo(expectedExceptionMessage)
        }
    }

    @Test
    fun simprintsInternalServerException_shouldWrapInSyncCloudIntegrationException() {
        runBlocking {
            mockSignedInUser(projectIdToSync, userIdToSync)
            mockSuccessfulLocalPeopleQueries(listOf(notYetSyncedPerson1))
            coEvery { eventRemoteDataSource.post(projectIdToSync, any()) } throws SyncCloudIntegrationException("", Throwable())

            assertThrows<SyncCloudIntegrationException> {
                withContext(Dispatchers.IO) {
                    personRepositoryUpSyncHelper.executeUploadWithProgress(this)
                }
            }
        }
    }

    @Test
    fun singleBatchOfPeople_uploadIt_shouldSucceed() {
        val peopleBatches = arrayOf(listOf(notYetSyncedPerson1, notYetSyncedPerson2))
        val events = createEventsFromPeople(
            arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2)
            )
        )

        testSuccessfulUpSync(
            localQueryResults = peopleBatches,
            expectedUploadBatches = events,
            expectedLocalUpdates = arrayOf(listOf(syncedPerson1, syncedPerson2))
        )
    }

    @Test
    fun moreThanBatchSizeFromLocal_uploadIt_shouldSucceedByCreatingBatches() {
        val peopleBatches = arrayOf(listOf(notYetSyncedPerson1, notYetSyncedPerson2, notYetSyncedPerson3))
        val events = createEventsFromPeople(
            arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2),
                listOf(notYetSyncedPerson3)
            )
        )

        testSuccessfulUpSync(
            localQueryResults = peopleBatches,
            expectedUploadBatches = events,
            expectedLocalUpdates = arrayOf(
                listOf(syncedPerson1, syncedPerson2),
                listOf(syncedPerson3)
            )
        )
    }

    @Test
    fun multipleBatchesFromLocal_uploadIt_shouldSucceed() {
        val peopleBatches = arrayOf(
            listOf(notYetSyncedPerson1, notYetSyncedPerson2),
            listOf(notYetSyncedPerson3)
        )
        val events = createEventsFromPeople(
            arrayOf(
                listOf(notYetSyncedPerson1, notYetSyncedPerson2),
                listOf(notYetSyncedPerson3)
            )
        )

        testSuccessfulUpSync(
            localQueryResults = peopleBatches,
            expectedUploadBatches = events,
            expectedLocalUpdates = arrayOf(
                listOf(syncedPerson1, syncedPerson2),
                listOf(syncedPerson3)
            )
        )
    }

    private fun setupBatchSize() {
        every { personRepositoryUpSyncHelper.batchSize } returns batchSize
    }

    private fun mockHelperToGenerateSameUuidForEvents() {
        every { personRepositoryUpSyncHelper.getRandomUuid() } returns "random_uuid"
    }

    private fun testSuccessfulUpSync(
        localQueryResults: Array<List<Subject>>,
        expectedUploadBatches: Array<Events>,
        expectedLocalUpdates: Array<List<Subject>>
    ) {
        runBlocking {

            mockSignedInUser(projectIdToSync, userIdToSync)
            mockSuccessfulLocalPeopleQueries(*localQueryResults)

            withContext(Dispatchers.IO) {
                personRepositoryUpSyncHelper.executeUploadWithProgress(this).testChannel()
                verifyLocalPeopleQueries()
                verifyPeopleUploads(expectedUploadBatches)
                verifyLocalPeopleUpdates(*expectedLocalUpdates)
            }
        }

    }

    private fun createEventsFromPeople(subjects: Array<List<Subject>>) =
        subjects.map {
            Events(it.map { createEventFromPerson(it) })
        }.toTypedArray()

    private fun createEventFromPerson(subject: Subject): Event =
        with(subject) {
            Event(
                "random_uuid",
                listOf(projectId),
                listOf(subjectId),
                listOf(attendantId),
                listOf(moduleId),
                modalities.map { it.toMode() },
                createPayload(subject)
            )
        }

    private fun createPayload(subject: Subject) =
        EnrolmentRecordCreationPayload(
            subjectId = subject.subjectId,
            projectId = subject.projectId,
            moduleId = subject.moduleId,
            attendantId = subject.attendantId,
            biometricReferences = buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
        )

    private fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>, faceSamples: List<FaceSample>) =
        listOf(
            FingerprintReference(
                fingerprintSamples.map {
                    FingerprintTemplate(it.templateQualityScore,
                        EncodingUtils.byteArrayToBase64(it.template),
                        it.fingerIdentifier.fromPersonToEvent())
                }),
            FaceReference(
                faceSamples.map {
                    FaceTemplate(
                        EncodingUtils.byteArrayToBase64(it.template)
                    )
                })
        )

    private fun mockSignedInUser(projectId: String, userId: String) {
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns projectId
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns userId
    }

    private fun mockSuccessfulLocalPeopleQueries(vararg queryResults: List<Subject>) {
        coEvery { subjectLocalDataSource.load(any()) } coAnswers {
            queryResults.fold(emptyList<Subject>()) { aggr, new -> aggr + new }.toList().asFlow()
        }
    }

    private fun verifyLocalPeopleQueries() {
        coVerify(exactly = 1) {
            subjectLocalDataSource.load(withArg {
                assertThat(it.toSync).isEqualTo(true)
            })
        }
    }

    private fun verifyPeopleUploads(events: Array<Events>) {
        events.forEach {
            coVerify(exactly = 1) { eventRemoteDataSource.post(projectIdToSync, it) }
        }
    }

    private fun verifyLocalPeopleUpdates(vararg updates: List<Subject>) {
        updates.forEach { update ->
            coVerify(exactly = 1) { subjectLocalDataSource.insertOrUpdate(update) }
        }
    }
}
