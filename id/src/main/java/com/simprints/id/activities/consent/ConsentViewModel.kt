package com.simprints.id.activities.consent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.ExternalScope
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.ConsentEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope,
    @DispatcherIO private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val configuration = MutableLiveData<ProjectConfiguration>()


    init {
        viewModelScope.launch(dispatcher) {
            //Adding "this" in front of livedata to fix NullSafeMutableLiveData lint issues
            this@ConsentViewModel.configuration.postValue(configManager.getProjectConfiguration())
        }
    }

    fun addConsentEvent(consentEvent: ConsentEvent) {
        externalScope.launch { eventRepository.addOrUpdateEvent(consentEvent) }
    }

    fun deleteLocationInfoFromSession() {
        externalScope.launch {
            val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()
            currentSessionEvent.payload.location = null
            eventRepository.addOrUpdateEvent(currentSessionEvent)
        }
    }
}
