package com.simprints.infra.sync.usecase.internal

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class ObserveEnrolmentRecordsCountUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) {
    internal operator fun invoke(): Flow<Int> = configRepository
        .observeProjectConfiguration()
        .map { it.projectId }
        .flatMapLatest { projectId ->
            enrolmentRecordRepository.observeCount(
                EnrolmentRecordQuery(projectId),
            )
        }.distinctUntilChanged()
}
