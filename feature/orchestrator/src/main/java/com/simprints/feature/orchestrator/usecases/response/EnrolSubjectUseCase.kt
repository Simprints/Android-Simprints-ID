package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EnrolSubjectUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    private val enrolmentRecordManager: EnrolmentRecordManager
) {

    suspend operator fun invoke(subject: Subject) {
        val currentSession = eventRepository.getCurrentCaptureSessionEvent().id
        val personCreationEvent = eventRepository.observeEventsFromSession(currentSession)
            .filterIsInstance<PersonCreationEvent>().first()

        eventRepository.addOrUpdateEvent(EnrolmentEventV2(
            timeHelper.now(),
            subject.subjectId,
            subject.projectId,
            subject.moduleId,
            subject.attendantId,
            personCreationEvent.id
        ))
        enrolmentRecordManager.performActions(listOf(SubjectAction.Creation(subject)))
    }

}
