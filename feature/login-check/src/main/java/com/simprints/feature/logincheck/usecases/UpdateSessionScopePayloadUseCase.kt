package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import javax.inject.Inject

internal class UpdateSessionScopePayloadUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val configRepository: ConfigRepository,
) {

    suspend operator fun invoke() {
        val configUpdatedAt = configRepository.getProjectConfiguration().updatedAt
        val recordCount = enrolmentRecordRepository.count()
        val sessionScope = eventRepository.getCurrentSessionScope()

        val updatedScope = sessionScope.copy(
            payload = sessionScope.payload.copy(
                projectConfigurationUpdatedAt = configUpdatedAt,
                databaseInfo = sessionScope.payload.databaseInfo.copy(
                    recordCount = recordCount
                )
            )
        )

        eventRepository.saveSessionScope(updatedScope)
    }
}
