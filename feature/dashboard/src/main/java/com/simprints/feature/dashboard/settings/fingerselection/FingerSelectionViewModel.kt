package com.simprints.feature.dashboard.settings.fingerselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Finger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FingerSelectionViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {

    val fingerSelections: LiveData<List<FingerSelectionItem>>
        get() = _fingerSelections
    private val _fingerSelections = MutableLiveData<List<FingerSelectionItem>>()

    fun start() {
        viewModelScope.launch {
            _fingerSelections.postValue(
                configRepository.getProjectConfiguration().fingerprint!!.bioSdkConfiguration.fingersToCapture
                    .toFingerSelectionItems()
            )
        }
    }

    private fun List<Finger>.toFingerSelectionItems(): List<FingerSelectionItem> {
        val result = mutableListOf<FingerSelectionItem>()
        this.forEach { finger ->
            val alreadyExistingFingerSelection = result.firstOrNull { fingerSelectionItem ->
                fingerSelectionItem.finger == finger
            }

            if (alreadyExistingFingerSelection != null) {
                alreadyExistingFingerSelection.quantity++
            } else {
                result.add(FingerSelectionItem(finger, 1))
            }
        }
        return result
    }
}

data class FingerSelectionItem(
    var finger: Finger,
    var quantity: Int
)
