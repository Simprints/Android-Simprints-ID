package com.simprints.id.activities.consent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.extentions.inBackground
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.ConsentEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    @DispatcherIO private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val consentConfiguration = MutableLiveData<ConsentConfiguration>()
    val modalities = MutableLiveData(listOf<GeneralConfiguration.Modality>())

    init {
        viewModelScope.launch {
            withContext(dispatcher) {
                consentConfiguration.postValue(configManager.getProjectConfiguration().consent)
                modalities.postValue(configManager.getProjectConfiguration().general.modalities)
            }
        }
    }

    fun addConsentEvent(consentEvent: ConsentEvent) {
        inBackground { eventRepository.addOrUpdateEvent(consentEvent) }
    }
}
