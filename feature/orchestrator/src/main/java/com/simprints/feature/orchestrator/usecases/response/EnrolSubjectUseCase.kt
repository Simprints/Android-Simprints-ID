package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.session.SessionEventRepository
import javax.inject.Inject

internal class EnrolSubjectUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) {
    suspend operator fun invoke(
        subject: Subject,
        project: Project,
    ) {
        val events = eventRepository
            .getEventsInCurrentSession()

        val biometricReferenceIds = events
            .filterIsInstance<BiometricReferenceCreationEvent>()
            .sortedByDescending { it.payload.createdAt }
            .map { it.payload.id }

        val externalCredentialIds = events
            .filterIsInstance<ExternalCredentialCaptureValueEvent>()
            .map { it.payload.id }

        eventRepository.addOrUpdateEvent(
            EnrolmentEventV4(
                createdAt = timeHelper.now(),
                subjectId = subject.subjectId,
                projectId = subject.projectId,
                moduleId = subject.moduleId,
                attendantId = subject.attendantId,
                biometricReferenceIds = biometricReferenceIds,
                externalCredentialIds = externalCredentialIds,
            ),
        )
        enrolmentRecordRepository.performActions(listOf(EnrolmentRecordAction.Creation(subject)), project)
    }
}
