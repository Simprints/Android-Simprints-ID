package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SimpleEventReporter @Inject constructor(
    private val coreEventRepository: SessionEventRepository,
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

    fun closeCurrentSessionNormally() {
        externalScope.launch {
            coreEventRepository.closeCurrentSession()
        }
    }

}

