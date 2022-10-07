package com.simprints.infra.enrolment.records.domain

import android.content.Context
import android.content.SharedPreferences
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.remote.EnrolmentRecordRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

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

    private val subjectRepository = mockk<SubjectRepository>()
    private val remoteDataSource = mockk<EnrolmentRecordRemoteDataSource>(relaxed = true)
    private val prefsEditor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val prefs = mockk<SharedPreferences> {
        every { edit() } returns prefsEditor
    }
    private val ctx = mockk<Context> {
        every { getSharedPreferences(any(), any()) } returns prefs
    }
    private val repository =
        EnrolmentRecordRepositoryImpl(ctx, remoteDataSource, subjectRepository, BATCH_SIZE)

    @Before
    fun setup() {
        every { prefsEditor.putString(any(), any()) } returns prefsEditor
        every { prefsEditor.remove(any()) } returns prefsEditor
    }

    @Test
    fun `should upload the records correctly when there is more than one batch`() = runTest {
        val expectedSubjectQuery = SubjectQuery(sort = true)
        every { prefs.getString(any(), null) } returns null
        coEvery { subjectRepository.load(expectedSubjectQuery) } returns flowOf(
            SUBJECT_1,
            SUBJECT_2,
            SUBJECT_3
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
        coEvery { subjectRepository.load(expectedSubjectQuery) } returns flowOf(
            SUBJECT_1,
            SUBJECT_2
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
        coEvery { subjectRepository.load(expectedSubjectQuery) } returns flowOf(
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
        coEvery { subjectRepository.load(expectedSubjectQuery) } returns flowOf(
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
            afterSubjectId = SUBJECT_ID_3
        )
        every { prefs.getString(any(), null) } returns SUBJECT_ID_3
        coEvery { subjectRepository.load(expectedSubjectQuery) } returns flowOf(
            SUBJECT_1,
            SUBJECT_2,
        )

        repository.uploadRecords(listOf())

        coVerify(exactly = 1) { remoteDataSource.uploadRecords(listOf(SUBJECT_1, SUBJECT_2)) }
        coVerify(exactly = 1) { prefsEditor.putString(any(), SUBJECT_ID_2) }
        coVerify(exactly = 1) { prefsEditor.remove(any()) }
    }
}
