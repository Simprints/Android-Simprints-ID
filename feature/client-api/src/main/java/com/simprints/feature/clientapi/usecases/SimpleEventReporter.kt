package com.simprints.feature.clientapi.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SimpleEventReporter @Inject constructor(
    private val coreEventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    fun addInvalidIntentEvent(
        action: String,
        extras: Map<String, Any>,
    ) {
        sessionCoroutineScope.launch {
            coreEventRepository.addOrUpdateEvent(InvalidIntentEvent(timeHelper.now(), action, extras))
        }
    }

    fun addCompletionCheckEvent(flowCompleted: Boolean) {
        sessionCoroutineScope.launch {
            coreEventRepository.addOrUpdateEvent(CompletionCheckEvent(timeHelper.now(), flowCompleted))
        }
    }

    fun closeCurrentSessionNormally() {
        sessionCoroutineScope.launch {
            coreEventRepository.closeCurrentSession()
        }
    }
}
