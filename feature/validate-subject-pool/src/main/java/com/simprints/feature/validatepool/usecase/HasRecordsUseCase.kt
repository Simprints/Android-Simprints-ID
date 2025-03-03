package com.simprints.feature.validatepool.usecase

import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import javax.inject.Inject

internal class HasRecordsUseCase @Inject constructor(
    private val enrolmentRepo: EnrolmentRecordRepository,
) {
    suspend operator fun invoke(subjectQuery: SubjectQuery) = enrolmentRepo.count(subjectQuery) > 0
}
