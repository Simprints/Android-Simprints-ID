package com.simprints.infra.enrolment.records.repository

import android.content.SharedPreferences
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.CandidateRecordBatch
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.repository.local.SelectEnrolmentRecordLocalDataSourceUseCase
import com.simprints.infra.enrolment.records.repository.local.migration.InsertRecordsInRoomDuringMigrationUseCase
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.security.SecurityManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class EnrolmentRecordRepositoryImplTest {
    companion object {
        private const val BATCH_SIZE = 2
        private const val SUBJECT_ID_1 = "SUBJECT_ID_1"
        private const val SUBJECT_ID_2 = "SUBJECT_ID_2"
        private const val SUBJECT_ID_3 = "SUBJECT_ID_3"
        private const val SUBJECT_ID_4 = "SUBJECT_ID_4"
        private const val SUBJECT_ID_5 = "SUBJECT_ID_5"
        private val ENROLMENT_RECORD_1 = mockk<EnrolmentRecord> {
            every { subjectId } returns SUBJECT_ID_1
        }
        private val ENROLMENT_RECORD_2 = mockk<EnrolmentRecord> {
            every { subjectId } returns SUBJECT_ID_2
        }
        private val ENROLMENT_RECORD_3 = mockk<EnrolmentRecord> {
            every { subjectId } returns SUBJECT_ID_3
        }
        private val ENROLMENT_RECORD_4 = mockk<EnrolmentRecord> {
            every { subjectId } returns SUBJECT_ID_4
        }
        private val ENROLMENT_RECORD_5 = mockk<EnrolmentRecord> {
            every { subjectId } returns SUBJECT_ID_5
        }
    }

    private val onCandidateLoaded: () -> Unit = {}
    private val tokenizationProcessor = mockk<TokenizationProcessor>()
    private val localDataSource = mockk<EnrolmentRecordLocalDataSource>(relaxed = true)
    private val selectEnrolmentRecordLocalDataSource = mockk<SelectEnrolmentRecordLocalDataSourceUseCase>()
    private val commCareDataSource = mockk<CandidateRecordDataSource>(relaxed = true)
    private val remoteDataSource = mockk<EnrolmentRecordRemoteDataSource>(relaxed = true)
    private val prefsEditor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val prefs = mockk<SharedPreferences> {
        every { edit() } returns prefsEditor
    }
    private val securityManager = mockk<SecurityManager> {
        every { buildEncryptedSharedPreferences(any()) } returns prefs
    }
    private lateinit var repository: EnrolmentRecordRepositoryImpl
    private val project = mockk<Project>()
    private val insertRecordsDuringMigration = mockk<InsertRecordsInRoomDuringMigrationUseCase>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        every { prefsEditor.putString(any(), any()) } returns prefsEditor
        every { prefsEditor.remove(any()) } returns prefsEditor
        coEvery { selectEnrolmentRecordLocalDataSource() } returns localDataSource
        repository = EnrolmentRecordRepositoryImpl(
            remoteDataSource = remoteDataSource,
            selectEnrolmentRecordLocalDataSource = selectEnrolmentRecordLocalDataSource,
            commCareDataSource = commCareDataSource,
            tokenizationProcessor = tokenizationProcessor,
            dispatcher = UnconfinedTestDispatcher(),
            batchSize = BATCH_SIZE,
            insertRecordsInRoomDuringMigration = insertRecordsDuringMigration,
            securityManager = securityManager,
        )
    }

    @Test
    fun `should upload the records correctly when there is more than one batch`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery(sort = true)
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedEnrolmentRecordQuery) } returns listOf(
            ENROLMENT_RECORD_1,
            ENROLMENT_RECORD_2,
            ENROLMENT_RECORD_3,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_1, ENROLMENT_RECORD_2)) }
        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_3)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when there is exactly one batch`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery(sort = true)
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedEnrolmentRecordQuery) } returns listOf(
            ENROLMENT_RECORD_1,
            ENROLMENT_RECORD_2,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_1, ENROLMENT_RECORD_2)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when there is more than two batches`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery(sort = true)
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedEnrolmentRecordQuery) } returns listOf(
            ENROLMENT_RECORD_1,
            ENROLMENT_RECORD_2,
            ENROLMENT_RECORD_3,
            ENROLMENT_RECORD_4,
            ENROLMENT_RECORD_5,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_1, ENROLMENT_RECORD_2)) }
        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_3, ENROLMENT_RECORD_4)) }
        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_5)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_4) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when some subject ids are specified`() = runTest {
        val expectedEnrolmentRecordQuery =
            EnrolmentRecordQuery(sort = true, subjectIds = listOf(SUBJECT_ID_1, SUBJECT_ID_2))
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedEnrolmentRecordQuery) } returns listOf(
            ENROLMENT_RECORD_1,
            ENROLMENT_RECORD_2,
        )

        repository.uploadRecords(listOf(SUBJECT_ID_1, SUBJECT_ID_2))

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_1, ENROLMENT_RECORD_2)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when it has failed before`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery(
            sort = true,
            afterSubjectId = SUBJECT_ID_3,
        )
        every { prefs.getString(any(), null) } returns SUBJECT_ID_3
        coEvery { localDataSource.load(expectedEnrolmentRecordQuery) } returns listOf(
            ENROLMENT_RECORD_1,
            ENROLMENT_RECORD_2,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(ENROLMENT_RECORD_1, ENROLMENT_RECORD_2)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `given the tokenization keys, when tokenizing existing subjects, the untokenized existing subjects should be tokenized`() =
        runTest {
            val projectId = "projectId"
            val attendantIdRaw = "attendantId".asTokenizableRaw()
            val moduleIdRaw = "moduleId".asTokenizableRaw()
            val attendantIdTokenized = "attendantId".asTokenizableEncrypted()
            val moduleIdTokenized = "moduleId".asTokenizableEncrypted()
            val project = mockk<Project>()
            val enrolmentRecord = EnrolmentRecord(
                subjectId = "subjectId",
                projectId = projectId,
                attendantId = attendantIdRaw,
                moduleId = moduleIdRaw,
                createdAt = Date(),
                updatedAt = null,
                references = emptyList(),
            )
            every { project.id } returns projectId
            coEvery { localDataSource.load(any()) } returns listOf(enrolmentRecord)
            every {
                tokenizationProcessor.tokenizeIfNecessary(
                    tokenizableString = attendantIdRaw,
                    tokenKeyType = TokenKeyType.AttendantId,
                    project = project,
                )
            } returns attendantIdTokenized
            every {
                tokenizationProcessor.tokenizeIfNecessary(
                    tokenizableString = moduleIdRaw,
                    tokenKeyType = TokenKeyType.ModuleId,
                    project = project,
                )
            } returns moduleIdTokenized

            repository.tokenizeExistingRecords(project)
            val expectedSubject = enrolmentRecord.copy(
                attendantId = attendantIdTokenized,
                moduleId = moduleIdTokenized,
            )
            val expectedEnrolmentRecordActions = listOf(EnrolmentRecordAction.Creation(expectedSubject))
            coVerify { localDataSource.performActions(expectedEnrolmentRecordActions, project) }
        }

    @Test
    fun `given the different project, when tokenizing existing subjects, the untokenized existing subjects are not tokenized`() = runTest {
        val projectId = "projectId"
        val attendantIdRaw = "attendantId".asTokenizableRaw()
        val moduleIdRaw = "moduleId".asTokenizableRaw()
        val project = mockk<Project>()
        val enrolmentRecord = EnrolmentRecord(
            subjectId = "subjectId",
            projectId = "another project id",
            attendantId = attendantIdRaw,
            moduleId = moduleIdRaw,
            createdAt = Date(),
            updatedAt = null,
            references = emptyList(),
        )
        every { project.id } returns projectId
        coEvery { localDataSource.load(any()) } returns listOf(enrolmentRecord)

        repository.tokenizeExistingRecords(project)
        coVerify { localDataSource.performActions(emptyList(), project) }
    }

    @Test
    fun `when tokenizing existing subjects throws exception, then it is captured and not thrown up the calling chain`() = runTest {
        val projectId = "projectId"
        val project = mockk<Project>()
        every { project.id } returns projectId
        coEvery { localDataSource.load(any()) } throws Exception()

        repository.tokenizeExistingRecords(project)
    }

    @Test
    fun `should return the correct count of subjects when dataSource is Simprints`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery()
        coEvery { localDataSource.count(expectedEnrolmentRecordQuery) } returns 5

        val count = repository.count(query = expectedEnrolmentRecordQuery, dataSource = BiometricDataSource.Simprints)

        assert(count == 5)
        coVerify(exactly = 1) { localDataSource.count(expectedEnrolmentRecordQuery) }
    }

    @Test
    fun `should return the correct count of subjects when dataSource is CommCare`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery()
        coEvery { commCareDataSource.count(expectedEnrolmentRecordQuery, any()) } returns 5

        val count = repository.count(query = expectedEnrolmentRecordQuery, dataSource = BiometricDataSource.CommCare(""))

        assert(count == 5)
        coVerify(exactly = 1) { commCareDataSource.count(expectedEnrolmentRecordQuery, any()) }
    }

    @Test
    fun `should forward the call to the local data source when loading fingerprint identities and dataSource is Simprints`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery()
        val expectedRange = listOf(0..10)
        val expectedFingerprintIdentities = listOf<CandidateRecord>()
        coEvery {
            localDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFingerprintIdentities)

        val fingerprintIdentities = mutableListOf<CandidateRecord>()
        repository
            .loadCandidateRecords(
                query = expectedEnrolmentRecordQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.Simprints,
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                fingerprintIdentities.addAll(it.identities)
            }

        assert(fingerprintIdentities == expectedFingerprintIdentities)
        coVerify(exactly = 1) {
            localDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        }
    }

    @Test
    fun `should forward the call to the commcare data source when loading fingerprint identities and dataSource is CommCare`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery()
        val expectedRange = listOf(0..10)
        val expectedFingerprintIdentities = listOf<CandidateRecord>()
        coEvery {
            commCareDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFingerprintIdentities)
        val fingerprintIdentities = mutableListOf<CandidateRecord>()
        repository
            .loadCandidateRecords(
                query = expectedEnrolmentRecordQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.CommCare(""),
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                fingerprintIdentities.addAll(it.identities)
            }

        assert(fingerprintIdentities == expectedFingerprintIdentities)
        coVerify(exactly = 1) {
            commCareDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        }
    }

    @Test
    fun `should forward the call to the local data source when loading face identities and dataSource is Simprints`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery()
        val expectedRange = listOf(0..10)
        val expectedFaceIdentities = listOf<CandidateRecord>()
        coEvery {
            localDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFaceIdentities)

        val faceIdentities = mutableListOf<CandidateRecord>()
        repository
            .loadCandidateRecords(
                query = expectedEnrolmentRecordQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.Simprints,
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                faceIdentities.addAll(it.identities)
            }

        assert(faceIdentities == expectedFaceIdentities)
        coVerify(exactly = 1) {
            localDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        }
    }

    @Test
    fun `should forward the call to the commcare data source when loading face identities and dataSource is CommCare`() = runTest {
        val expectedEnrolmentRecordQuery = EnrolmentRecordQuery()
        val expectedRange = listOf(0..10)
        val expectedFaceIdentities = listOf<CandidateRecord>()
        coEvery {
            commCareDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFaceIdentities)

        val faceIdentities = mutableListOf<CandidateRecord>()

        repository
            .loadCandidateRecords(
                query = expectedEnrolmentRecordQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.CommCare(""),
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                faceIdentities.addAll(it.identities)
            }

        assert(faceIdentities == expectedFaceIdentities)
        coVerify(exactly = 1) {
            commCareDataSource.loadCandidateRecords(
                expectedEnrolmentRecordQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        }
    }

    private fun createTestChannel(vararg lists: List<CandidateRecord>): ReceiveChannel<CandidateRecordBatch> {
        val channel = Channel<CandidateRecordBatch>(lists.size)
        runBlocking {
            var time = 0L
            for (list in lists) {
                channel.send(
                    CandidateRecordBatch(
                        identities = list,
                        loadingStartTime = Timestamp(time++),
                        loadingEndTime = Timestamp(time++),
                    ),
                )
            }
            channel.close()
        }
        return channel
    }

    @Test
    fun `performActions should forward the subject creation calls to the insertRecordsDuringMigration`() = runTest {
        val actions = listOf(
            mockk<EnrolmentRecordAction.Creation>(),
            mockk<EnrolmentRecordAction.Deletion>(),
            mockk<EnrolmentRecordAction.Update>(),
            mockk<EnrolmentRecordAction.Creation>(),
        )
        coJustRun { insertRecordsDuringMigration.invoke(any(), any()) }
        repository.performActions(actions, mockk())
        coVerify { insertRecordsDuringMigration.invoke(any(), any()) }
    }
}
