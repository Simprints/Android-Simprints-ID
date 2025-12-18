package com.simprints.feature.externalcredential.usecase

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.extentions.isValidGuid
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordAction
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class ResetExternalCredentialsInSessionUseCase @Inject() constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val configManager: ConfigManager,
    private val eventRepository: SessionEventRepository,
    @param:SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    suspend operator fun invoke(
        scannedCredential: ScannedCredential? = null,
        subjectId: String = "",
    ) {
        val enrolmentUpdateEvents = eventRepository
            .getEventsInCurrentSession()
            .filterIsInstance<EnrolmentUpdateEvent>()

        // Within a session the external credentials can be linked to a single subject only,
        // therefore we must ensure that on consecutive confirmation the previous links are reverted.
        val credentialsToRemove = enrolmentUpdateEvents.map {
            EnrolmentRecordAction.Update(
                subjectId = it.payload.subjectId,
                samplesToAdd = emptyList(),
                referenceIdsToRemove = emptyList(),
                externalCredentialsToAdd = emptyList(),
                externalCredentialIdsToRemove = it.payload.externalCredentialIdsToAdd,
            )
        }

        val validSubjectId = subjectId.takeIf { it.isValidGuid() }
        val credentialsToAdd = if (validSubjectId != null && scannedCredential != null) {
            listOf(
                EnrolmentRecordAction.Update(
                    subjectId = subjectId,
                    samplesToAdd = emptyList(),
                    referenceIdsToRemove = emptyList(),
                    externalCredentialsToAdd = listOf(scannedCredential.toExternalCredential(validSubjectId)),
                    externalCredentialIdsToRemove = emptyList(),
                ),
            )
        } else {
            emptyList()
        }

        configManager.getProject()?.let { project ->
            val updateActions = credentialsToRemove + credentialsToAdd
            enrolmentRecordRepository.performActions(updateActions, project)
        }

        // Since we are potentially linking the credentials to a new subject, previous updates must be deleted
        with(sessionCoroutineScope) { eventRepository.deleteEvents(enrolmentUpdateEvents) }
    }
}
