package com.simprints.feature.logincheck.usecases

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.session.SessionEventRepository
import javax.inject.Inject

internal class UpdateProjectInCurrentSessionUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
) {
    suspend operator fun invoke() {
        val sessionScope = eventRepository.getCurrentSessionScope()
        val signedProjectId = authStore.signedInProjectId

        if (signedProjectId != sessionScope.projectId) {
            val updatedSessionScope = sessionScope.copy(
                projectId = signedProjectId,
                payload = sessionScope.payload.copy(
                    modalities = configManager.getProjectConfiguration().general.modalities,
                    language = configManager.getDeviceConfiguration().language,
                ),
            )

            eventRepository.saveSessionScope(updatedSessionScope)
        }

        // Calling addOrUpdate will update the project ID of the event
        val associatedEvents = eventRepository.getEventsInCurrentSession()
        associatedEvents.forEach { eventRepository.addOrUpdateEvent(it) }
    }
}
