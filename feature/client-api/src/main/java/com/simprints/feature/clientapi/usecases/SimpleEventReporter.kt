package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SimpleEventReporter @Inject constructor(
    private val coreEventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    @ExternalScope private val externalScope: CoroutineScope
) {

    fun addInvalidIntentEvent(action: String, extras: Map<String, Any>) {
        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(InvalidIntentEvent(timeHelper.now(), action, extras))
        }
    }

    fun addCompletionCheckEvent(flowCompleted: Boolean) {
        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(CompletionCheckEvent(timeHelper.now(), flowCompleted))
        }
    }

    suspend fun closeCurrentSessionNormally() {
        coreEventRepository.closeCurrentSession()
    }

}

