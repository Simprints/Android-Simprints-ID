package com.simprints.infra.enrolment.records.sync

import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.SubjectRepository
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.sync.worker.EnrolmentRecordScheduler
import java.io.Serializable
import javax.inject.Inject

internal class EnrolmentRecordManagerImpl @Inject constructor(
    private val enrolmentRecordScheduler: EnrolmentRecordScheduler,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) : EnrolmentRecordManager {

    override fun upload(id: String, subjectIds: List<String>) =
        enrolmentRecordScheduler.upload(id, subjectIds)

    override suspend fun uploadRecords(subjectIds: List<String>) = enrolmentRecordRepository.uploadRecords(subjectIds)

    override suspend fun load(query: SubjectQuery): List<Subject> =
        enrolmentRecordRepository.load(query)

    override suspend fun delete(queries: List<SubjectQuery>) = enrolmentRecordRepository.delete(queries)

    override suspend fun deleteAll() = enrolmentRecordRepository.deleteAll()

    override suspend fun count(query: SubjectQuery): Int = enrolmentRecordRepository.count(query)

    override suspend fun performActions(actions: List<SubjectAction>) = enrolmentRecordRepository.performActions(actions)
}
