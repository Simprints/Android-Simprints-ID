package com.simprints.feature.logincheck.usecases

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.session.SessionEventRepository
import javax.inject.Inject

internal class UpdateProjectInCurrentSessionUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val authStore: AuthStore,
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke() {
        val sessionScope = eventRepository.getCurrentSessionScope()
        val signedProjectId = authStore.signedInProjectId

        if (signedProjectId != sessionScope.projectId) {
            val updatedSessionScope = sessionScope.copy(
                projectId = signedProjectId,
                payload = sessionScope.payload.copy(
                    modalities = configRepository.getProjectConfiguration().general.modalities,
                    language = configRepository.getDeviceConfiguration().language,
                ),
            )

            eventRepository.saveSessionScope(updatedSessionScope)
        }

        // Calling addOrUpdate will update the project ID of the event
        val associatedEvents = eventRepository.getEventsInCurrentSession()
        associatedEvents.forEach { eventRepository.addOrUpdateEvent(it) }
    }
}
