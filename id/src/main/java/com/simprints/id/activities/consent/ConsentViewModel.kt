package com.simprints.id.activities.consent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.ConsentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
//Livedata is throwing NullSafeMutableLiveData which is unreal problem
//For more information, please check this issue https://issuetracker.google.com/issues/214428166
@Suppress("NullSafeMutableLiveData")
class ConsentViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    val configuration = MutableLiveData<ProjectConfiguration>()


    init {
        viewModelScope.launch {
            configuration.postValue(configManager.getProjectConfiguration())
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
