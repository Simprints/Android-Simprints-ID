package com.simprints.infra.enrolment.records.repository

import android.content.SharedPreferences
import com.simprints.core.domain.sample.Identity
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
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
        private val SUBJECT_1 = mockk<Subject> {
            every { subjectId } returns SUBJECT_ID_1
        }
        private val SUBJECT_2 = mockk<Subject> {
            every { subjectId } returns SUBJECT_ID_2
        }
        private val SUBJECT_3 = mockk<Subject> {
            every { subjectId } returns SUBJECT_ID_3
        }
        private val SUBJECT_4 = mockk<Subject> {
            every { subjectId } returns SUBJECT_ID_4
        }
        private val SUBJECT_5 = mockk<Subject> {
            every { subjectId } returns SUBJECT_ID_5
        }
    }

    private val onCandidateLoaded: () -> Unit = {}
    private val tokenizationProcessor = mockk<TokenizationProcessor>()
    private val localDataSource = mockk<EnrolmentRecordLocalDataSource>(relaxed = true)
    private val selectEnrolmentRecordLocalDataSource = mockk<SelectEnrolmentRecordLocalDataSourceUseCase>()
    private val commCareDataSource = mockk<IdentityDataSource>(relaxed = true)
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
        val expectedSubjectQuery = SubjectQuery(sort = true)
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedSubjectQuery) } returns listOf(
            SUBJECT_1,
            SUBJECT_2,
            SUBJECT_3,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_1, SUBJECT_2)) }
        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_3)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when there is exactly one batch`() = runTest {
        val expectedSubjectQuery = SubjectQuery(sort = true)
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedSubjectQuery) } returns listOf(
            SUBJECT_1,
            SUBJECT_2,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_1, SUBJECT_2)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when there is more than two batches`() = runTest {
        val expectedSubjectQuery = SubjectQuery(sort = true)
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedSubjectQuery) } returns listOf(
            SUBJECT_1,
            SUBJECT_2,
            SUBJECT_3,
            SUBJECT_4,
            SUBJECT_5,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_1, SUBJECT_2)) }
        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_3, SUBJECT_4)) }
        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_5)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_4) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when some subject ids are specified`() = runTest {
        val expectedSubjectQuery =
            SubjectQuery(sort = true, subjectIds = listOf(SUBJECT_ID_1, SUBJECT_ID_2))
        every { prefs.getString(any(), null) } returns null
        coEvery { localDataSource.load(expectedSubjectQuery) } returns listOf(
            SUBJECT_1,
            SUBJECT_2,
        )

        repository.uploadRecords(listOf(SUBJECT_ID_1, SUBJECT_ID_2))

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_1, SUBJECT_2)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }

    @Test
    fun `should upload the records correctly when it has failed before`() = runTest {
        val expectedSubjectQuery = SubjectQuery(
            sort = true,
            afterSubjectId = SUBJECT_ID_3,
        )
        every { prefs.getString(any(), null) } returns SUBJECT_ID_3
        coEvery { localDataSource.load(expectedSubjectQuery) } returns listOf(
            SUBJECT_1,
            SUBJECT_2,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_1, SUBJECT_2)) }
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
            val subject = Subject(
                subjectId = "subjectId",
                projectId = projectId,
                attendantId = attendantIdRaw,
                moduleId = moduleIdRaw,
                createdAt = Date(),
                updatedAt = null,
                samples = emptyList(),
            )
            every { project.id } returns projectId
            coEvery { localDataSource.load(any()) } returns listOf(subject)
            every {
                tokenizationProcessor.encrypt(
                    decrypted = attendantIdRaw,
                    tokenKeyType = TokenKeyType.AttendantId,
                    project = project,
                )
            } returns attendantIdTokenized
            every {
                tokenizationProcessor.encrypt(
                    decrypted = moduleIdRaw,
                    tokenKeyType = TokenKeyType.ModuleId,
                    project = project,
                )
            } returns moduleIdTokenized

            repository.tokenizeExistingRecords(project)
            val expectedSubject = subject.copy(
                attendantId = attendantIdTokenized,
                moduleId = moduleIdTokenized,
            )
            val expectedSubjectActions = listOf(SubjectAction.Creation(expectedSubject))
            coVerify { localDataSource.performActions(expectedSubjectActions, project) }
        }

    @Test
    fun `given the different project, when tokenizing existing subjects, the untokenized existing subjects are not tokenized`() = runTest {
        val projectId = "projectId"
        val attendantIdRaw = "attendantId".asTokenizableRaw()
        val moduleIdRaw = "moduleId".asTokenizableRaw()
        val project = mockk<Project>()
        val subject = Subject(
            subjectId = "subjectId",
            projectId = "another project id",
            attendantId = attendantIdRaw,
            moduleId = moduleIdRaw,
            createdAt = Date(),
            updatedAt = null,
            samples = emptyList(),
        )
        every { project.id } returns projectId
        coEvery { localDataSource.load(any()) } returns listOf(subject)

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
        val expectedSubjectQuery = SubjectQuery()
        coEvery { localDataSource.count(expectedSubjectQuery) } returns 5

        val count = repository.count(query = expectedSubjectQuery, dataSource = BiometricDataSource.Simprints)

        assert(count == 5)
        coVerify(exactly = 1) { localDataSource.count(expectedSubjectQuery) }
    }

    @Test
    fun `should return the correct count of subjects when dataSource is CommCare`() = runTest {
        val expectedSubjectQuery = SubjectQuery()
        coEvery { commCareDataSource.count(expectedSubjectQuery, any()) } returns 5

        val count = repository.count(query = expectedSubjectQuery, dataSource = BiometricDataSource.CommCare(""))

        assert(count == 5)
        coVerify(exactly = 1) { commCareDataSource.count(expectedSubjectQuery, any()) }
    }

    @Test
    fun `should forward the call to the local data source when loading fingerprint identities and dataSource is Simprints`() = runTest {
        val expectedSubjectQuery = SubjectQuery()
        val expectedRange = listOf(0..10)
        val expectedFingerprintIdentities = listOf<Identity>()
        coEvery {
            localDataSource.loadIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFingerprintIdentities)

        val fingerprintIdentities = mutableListOf<Identity>()
        repository
            .loadIdentities(
                query = expectedSubjectQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.Simprints,
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                fingerprintIdentities.addAll(it)
            }

        assert(fingerprintIdentities == expectedFingerprintIdentities)
        coVerify(exactly = 1) {
            localDataSource.loadIdentities(
                expectedSubjectQuery,
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
        val expectedSubjectQuery = SubjectQuery()
        val expectedRange = listOf(0..10)
        val expectedFingerprintIdentities = listOf<Identity>()
        coEvery {
            commCareDataSource.loadIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFingerprintIdentities)
        val fingerprintIdentities = mutableListOf<Identity>()
        repository
            .loadIdentities(
                query = expectedSubjectQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.CommCare(""),
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                fingerprintIdentities.addAll(it)
            }

        assert(fingerprintIdentities == expectedFingerprintIdentities)
        coVerify(exactly = 1) {
            commCareDataSource.loadIdentities(
                expectedSubjectQuery,
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
        val expectedSubjectQuery = SubjectQuery()
        val expectedRange = listOf(0..10)
        val expectedFaceIdentities = listOf<Identity>()
        coEvery {
            localDataSource.loadIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFaceIdentities)

        val faceIdentities = mutableListOf<Identity>()
        repository
            .loadIdentities(
                query = expectedSubjectQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.Simprints,
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                faceIdentities.addAll(it)
            }

        assert(faceIdentities == expectedFaceIdentities)
        coVerify(exactly = 1) {
            localDataSource.loadIdentities(
                expectedSubjectQuery,
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
        val expectedSubjectQuery = SubjectQuery()
        val expectedRange = listOf(0..10)
        val expectedFaceIdentities = listOf<Identity>()
        coEvery {
            commCareDataSource.loadIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        } returns createTestChannel(expectedFaceIdentities)

        val faceIdentities = mutableListOf<Identity>()

        repository
            .loadIdentities(
                query = expectedSubjectQuery,
                ranges = expectedRange,
                dataSource = BiometricDataSource.CommCare(""),
                project = project,
                scope = this,
                onCandidateLoaded = onCandidateLoaded,
            ).consumeEach {
                faceIdentities.addAll(it)
            }

        assert(faceIdentities == expectedFaceIdentities)
        coVerify(exactly = 1) {
            commCareDataSource.loadIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                this@runTest,
                onCandidateLoaded,
            )
        }
    }

    private fun <T> createTestChannel(vararg lists: List<T>): ReceiveChannel<List<T>> {
        val channel = Channel<List<T>>(lists.size)
        runBlocking {
            for (list in lists) {
                channel.send(list)
            }
            channel.close()
        }
        return channel
    }

    @Test
    fun `performActions should forward the subject creation calls to the insertRecordsDuringMigration`() = runTest {
        val actions = listOf<SubjectAction>(
            mockk<SubjectAction.Creation>(),
            mockk<SubjectAction.Deletion>(),
            mockk<SubjectAction.Update>(),
            mockk<SubjectAction.Creation>(),
        )
        coJustRun { insertRecordsDuringMigration.invoke(any(), any()) }
        repository.performActions(actions, mockk())
        coVerify { insertRecordsDuringMigration.invoke(any(), any()) }
    }
}
