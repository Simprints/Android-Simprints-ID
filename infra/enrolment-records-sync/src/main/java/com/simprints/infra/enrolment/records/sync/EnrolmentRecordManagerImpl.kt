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
    private val subjectRepository: SubjectRepository,
) : EnrolmentRecordManager {

    override fun upload(id: String, subjectIds: List<String>) =
        enrolmentRecordScheduler.upload(id, subjectIds)

    override suspend fun loadFaceIdentities(query: Serializable): List<FaceIdentity> =
        subjectRepository.loadFaceIdentities(query)

    override suspend fun loadFingerprintIdentities(query: Serializable): List<FingerprintIdentity> =
        subjectRepository.loadFingerprintIdentities(query)

    override suspend fun uploadRecords(subjectIds: List<String>) = enrolmentRecordRepository.uploadRecords(subjectIds)

    override suspend fun load(query: SubjectQuery): List<Subject> =
        subjectRepository.load(query)

    override suspend fun delete(queries: List<SubjectQuery>) = subjectRepository.delete(queries)

    override suspend fun deleteAll() = subjectRepository.deleteAll()

    override suspend fun count(query: SubjectQuery): Int = subjectRepository.count(query)

    override suspend fun performActions(actions: List<SubjectAction>) = subjectRepository.performActions(actions)
}
