package com.simprints.feature.logincheck.usecases

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.EventRepository
import javax.inject.Inject

internal class UpdateProjectInCurrentSessionUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val authStore: AuthStore,
    private val configRepository: ConfigRepository,
) {

    suspend operator fun invoke() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()

        val signedProjectId = authStore.signedInProjectId
        if (signedProjectId != currentSessionEvent.payload.projectId) {
            val projectConfiguration = configRepository.getProjectConfiguration()
            currentSessionEvent.updateProjectId(signedProjectId)
            currentSessionEvent.updateModalities(projectConfiguration.general.modalities)
            val deviceConfiguration = configRepository.getDeviceConfiguration()
            currentSessionEvent.updateLanguage(deviceConfiguration.language)
            eventRepository.addOrUpdateEvent(currentSessionEvent)
        }

        val associatedEvents = eventRepository.observeEventsFromSession(currentSessionEvent.id)
        associatedEvents.collect {
            it.labels = it.labels.copy(projectId = signedProjectId)
            eventRepository.addOrUpdateEvent(it)
        }
    }
}
