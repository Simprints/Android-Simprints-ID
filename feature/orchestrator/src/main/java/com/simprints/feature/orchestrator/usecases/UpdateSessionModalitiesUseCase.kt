package com.simprints.feature.orchestrator.usecases

import com.simprints.core.ExternalScope
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class UpdateSessionModalitiesUseCase @Inject constructor(
    private val sessionEventRepository: SessionEventRepository,
    @ExternalScope private val externalScope: CoroutineScope,
) {
    operator fun invoke(modalities: List<Modality>) {
        // Empty modalities is invalid so don't update
        if (modalities.isEmpty()) return
         externalScope.launch {
             val sessionScope = sessionEventRepository.getCurrentSessionScope()
             val updatedSessionScope = sessionScope.copy(
                 payload = sessionScope.payload.copy(
                     modalities = modalities,
                 )
             )
             sessionEventRepository.saveSessionScope(updatedSessionScope)
         }
    }
}
