package com.simprints.infra.enrolment.records

import com.simprints.infra.enrolment.records.domain.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.domain.SubjectRepository
import com.simprints.infra.enrolment.records.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.worker.EnrolmentRecordScheduler
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EnrolmentRecordManagerImplTest {

    companion object {
        private const val SUBJECT_ID_1 = "subject1"
        private val QUERY = SubjectQuery(projectId = "project")
        private val ACTION = SubjectAction.Deletion("subject")
    }

    private val enrolmentRecordScheduler = mockk<EnrolmentRecordScheduler>(relaxed = true)
    private val subjectRepository = mockk<SubjectRepository>(relaxed = true)
    private val enrolmentRecordRepository = mockk<EnrolmentRecordRepository>(relaxed = true)
    private val enrolmentRecordManager =
        EnrolmentRecordManagerImpl(
            enrolmentRecordScheduler,
            enrolmentRecordRepository,
            subjectRepository
        )

    @Test
    fun `upload should call the correct method`() = runTest {
        enrolmentRecordManager.upload("id", listOf(SUBJECT_ID_1))

        coVerify(exactly = 1) { enrolmentRecordScheduler.upload("id", listOf(SUBJECT_ID_1)) }
    }

    @Test
    fun `uploadRecords should call the correct method`() = runTest {
        enrolmentRecordManager.uploadRecords(listOf(SUBJECT_ID_1))

        coVerify(exactly = 1) { enrolmentRecordRepository.uploadRecords(listOf(SUBJECT_ID_1)) }
    }

    @Test
    fun `loadFaceIdentities should call the correct method`() = runTest {
        enrolmentRecordManager.loadFaceIdentities(QUERY)

        coVerify(exactly = 1) { subjectRepository.loadFaceIdentities(QUERY) }
    }

    @Test
    fun `loadFingerprintIdentities should call the correct method`() = runTest {
        enrolmentRecordManager.loadFingerprintIdentities(QUERY)

        coVerify(exactly = 1) { subjectRepository.loadFingerprintIdentities(QUERY) }
    }

    @Test
    fun `load should call the correct method`() = runTest {
        enrolmentRecordManager.load(QUERY)

        coVerify(exactly = 1) { subjectRepository.load(QUERY) }
    }

    @Test
    fun `delete should call the correct method`() = runTest {
        enrolmentRecordManager.delete(listOf(QUERY))

        coVerify(exactly = 1) { subjectRepository.delete(listOf(QUERY)) }
    }

    @Test
    fun `deleteAll() should call the correct method`() = runTest {
        enrolmentRecordManager.deleteAll()

        coVerify(exactly = 1) { subjectRepository.deleteAll() }
    }

    @Test
    fun `count should call the correct method`() = runTest {
        enrolmentRecordManager.count(QUERY)

        coVerify(exactly = 1) { subjectRepository.count(QUERY) }
    }

    @Test
    fun `performActions should call the correct method`() = runTest {
        enrolmentRecordManager.performActions(listOf(ACTION))

        coVerify(exactly = 1) { subjectRepository.performActions(listOf(ACTION)) }
    }
}
