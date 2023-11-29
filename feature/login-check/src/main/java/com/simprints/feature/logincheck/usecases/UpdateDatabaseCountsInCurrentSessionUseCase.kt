package com.simprints.feature.logincheck.usecases

import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import javax.inject.Inject

internal class UpdateDatabaseCountsInCurrentSessionUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository
) {
    suspend operator fun invoke() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()

        val payload = currentSessionEvent.payload
        payload.databaseInfo.recordCount = enrolmentRecordRepository.count()

        eventRepository.addOrUpdateEvent(currentSessionEvent)
    }
}
