package com.simprints.id.activities.coreexitform

import androidx.lifecycle.ViewModel
import com.simprints.core.ExternalScope
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoreExitFormViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope
) :
    ViewModel() {

    fun addExitFormEvent(
        startTime: Long,
        endTime: Long,
        otherText: String,
        coreExitFormReason: CoreExitFormReason
    ) {
        externalScope.launch {
            eventRepository.addOrUpdateEvent(
                RefusalEvent(
                    startTime,
                    endTime,
                    coreExitFormReason.toRefusalEventAnswer(),
                    otherText
                )
            )
        }
    }
}
