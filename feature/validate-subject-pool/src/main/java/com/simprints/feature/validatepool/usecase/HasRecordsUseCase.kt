package com.simprints.feature.validatepool.usecase

import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import javax.inject.Inject

internal class HasRecordsUseCase @Inject constructor(
    private val enrolmentRepo: EnrolmentRecordRepository,
) {
    suspend operator fun invoke(enrolmentRecordQuery: EnrolmentRecordQuery) = enrolmentRepo.count(enrolmentRecordQuery) > 0
}
