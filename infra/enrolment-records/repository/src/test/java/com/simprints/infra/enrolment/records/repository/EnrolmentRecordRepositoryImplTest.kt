package com.simprints.infra.enrolment.records.repository

import android.content.Context
import android.content.SharedPreferences
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
    private val commCareDataSource = mockk<IdentityDataSource>(relaxed = true)
    private val remoteDataSource = mockk<EnrolmentRecordRemoteDataSource>(relaxed = true)
    private val prefsEditor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val prefs = mockk<SharedPreferences> {
        every { edit() } returns prefsEditor
    }
    private val ctx = mockk<Context> {
        every { getSharedPreferences(any(), any()) } returns prefs
    }
    private lateinit var repository: EnrolmentRecordRepositoryImpl
    private val project = mockk<Project>()

    @Before
    fun setup() {
        every { prefsEditor.putString(any(), any()) } returns prefsEditor
        every { prefsEditor.remove(any()) } returns prefsEditor

        repository = EnrolmentRecordRepositoryImpl(
            context = ctx,
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            commCareDataSource = commCareDataSource,
            tokenizationProcessor = tokenizationProcessor,
            dispatcher = UnconfinedTestDispatcher(),
            batchSize = BATCH_SIZE,
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
                fingerprintSamples = emptyList(),
                faceSamples = emptyList(),
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
            fingerprintSamples = emptyList(),
            faceSamples = emptyList(),
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
        val expectedRange = 0..10
        val expectedFingerprintIdentities = listOf<FingerprintIdentity>()
        coEvery {
            localDataSource.loadFingerprintIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                onCandidateLoaded,
            )
        } returns expectedFingerprintIdentities

        val fingerprintIdentities = repository.loadFingerprintIdentities(
            query = expectedSubjectQuery,
            range = expectedRange,
            dataSource = BiometricDataSource.Simprints,
            project = project,
            onCandidateLoaded = onCandidateLoaded,
        )

        assert(fingerprintIdentities == expectedFingerprintIdentities)
        coVerify(exactly = 1) {
            localDataSource.loadFingerprintIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                onCandidateLoaded,
            )
        }
    }

    @Test
    fun `should forward the call to the commcare data source when loading fingerprint identities and dataSource is CommCare`() = runTest {
        val expectedSubjectQuery = SubjectQuery()
        val expectedRange = 0..10
        val expectedFingerprintIdentities = listOf<FingerprintIdentity>()
        coEvery {
            commCareDataSource.loadFingerprintIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                onCandidateLoaded,
            )
        } returns expectedFingerprintIdentities

        val fingerprintIdentities = repository.loadFingerprintIdentities(
            query = expectedSubjectQuery,
            range = expectedRange,
            dataSource = BiometricDataSource.CommCare(""),
            project = project,
            onCandidateLoaded = onCandidateLoaded,
        )

        assert(fingerprintIdentities == expectedFingerprintIdentities)
        coVerify(exactly = 1) {
            commCareDataSource.loadFingerprintIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                onCandidateLoaded,
            )
        }
    }

    @Test
    fun `should forward the call to the local data source when loading face identities and dataSource is Simprints`() = runTest {
        val expectedSubjectQuery = SubjectQuery()
        val expectedRange = 0..10
        val expectedFaceIdentities = listOf<FaceIdentity>()
        coEvery {
            localDataSource.loadFaceIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                onCandidateLoaded,
            )
        } returns expectedFaceIdentities

        val faceIdentities = repository.loadFaceIdentities(
            query = expectedSubjectQuery,
            range = expectedRange,
            dataSource = BiometricDataSource.Simprints,
            project = project,
            onCandidateLoaded = onCandidateLoaded,
        )

        assert(faceIdentities == expectedFaceIdentities)
        coVerify(exactly = 1) { localDataSource.loadFaceIdentities(expectedSubjectQuery, expectedRange, any(), project, onCandidateLoaded) }
    }

    @Test
    fun `should forward the call to the commcare data source when loading face identities and dataSource is CommCare`() = runTest {
        val expectedSubjectQuery = SubjectQuery()
        val expectedRange = 0..10
        val expectedFaceIdentities = listOf<FaceIdentity>()
        coEvery {
            commCareDataSource.loadFaceIdentities(expectedSubjectQuery, expectedRange, any(), project, onCandidateLoaded)
        } returns expectedFaceIdentities

        val faceIdentities = repository.loadFaceIdentities(
            query = expectedSubjectQuery,
            range = expectedRange,
            dataSource = BiometricDataSource.CommCare(""),
            project = project,
            onCandidateLoaded = onCandidateLoaded,
        )

        assert(faceIdentities == expectedFaceIdentities)
        coVerify(exactly = 1) {
            commCareDataSource.loadFaceIdentities(
                expectedSubjectQuery,
                expectedRange,
                any(),
                project,
                onCandidateLoaded,
            )
        }
    }
}
