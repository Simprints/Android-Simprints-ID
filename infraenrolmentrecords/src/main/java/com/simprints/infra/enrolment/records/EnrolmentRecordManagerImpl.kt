package com.simprints.infra.enrolment.records

import com.simprints.infra.enrolment.records.domain.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.domain.SubjectRepository
import com.simprints.infra.enrolment.records.domain.models.*
import com.simprints.infra.enrolment.records.worker.EnrolmentRecordScheduler
import kotlinx.coroutines.flow.Flow
import java.io.Serializable
import javax.inject.Inject

internal class EnrolmentRecordManagerImpl @Inject constructor(
    private val enrolmentRecordScheduler: EnrolmentRecordScheduler,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val subjectRepository: SubjectRepository
) : EnrolmentRecordManager {

    override fun upload(id: String, subjectIds: List<String>) =
        enrolmentRecordScheduler.upload(id, subjectIds)

    override suspend fun loadFaceIdentities(query: Serializable): Flow<FaceIdentity> =
        subjectRepository.loadFaceIdentities(query)

    override suspend fun loadFingerprintIdentities(query: Serializable): Flow<FingerprintIdentity> =
        subjectRepository.loadFingerprintIdentities(query)

    override suspend fun uploadRecords(subjectIds: List<String>) =
        enrolmentRecordRepository.uploadRecords(subjectIds)

    override suspend fun load(query: SubjectQuery): Flow<Subject> =
        subjectRepository.load(query)

    override suspend fun delete(queries: List<SubjectQuery>) =
        subjectRepository.delete(queries)

    override suspend fun deleteAll() =
        subjectRepository.deleteAll()

    override suspend fun count(query: SubjectQuery): Int =
        subjectRepository.count(query)

    override suspend fun performActions(actions: List<SubjectAction>) =
        subjectRepository.performActions(actions)
}
