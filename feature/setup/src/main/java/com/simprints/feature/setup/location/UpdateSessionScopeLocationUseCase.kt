package com.simprints.feature.setup.location

import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.events.session.SessionEventRepository
import javax.inject.Inject

internal class UpdateSessionScopeLocationUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(location: Location) {
        val sessionScope = eventRepository.getCurrentSessionScope()
        val updatedSessionScope = sessionScope.copy(
            payload = sessionScope.payload.copy(
                location = location,
            ),
        )
        eventRepository.saveSessionScope(updatedSessionScope)
    }
}
