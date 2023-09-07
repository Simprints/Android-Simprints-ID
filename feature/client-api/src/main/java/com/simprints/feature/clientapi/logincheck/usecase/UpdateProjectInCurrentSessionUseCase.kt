package com.simprints.feature.clientapi.logincheck.usecase

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.events.EventRepository
import javax.inject.Inject

internal class UpdateProjectInCurrentSessionUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
) {

    suspend operator fun invoke() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()

        val signedProjectId = authStore.signedInProjectId
        if (signedProjectId != currentSessionEvent.payload.projectId) {
            val projectConfiguration = configManager.getProjectConfiguration()
            currentSessionEvent.updateProjectId(signedProjectId)
            currentSessionEvent.updateModalities(projectConfiguration.general.modalities)
            eventRepository.addOrUpdateEvent(currentSessionEvent)
        }

        val associatedEvents = eventRepository.observeEventsFromSession(currentSessionEvent.id)
        associatedEvents.collect {
            it.labels = it.labels.copy(projectId = signedProjectId)
            eventRepository.addOrUpdateEvent(it)
        }
    }
}
