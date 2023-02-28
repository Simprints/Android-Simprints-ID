package com.simprints.feature.dashboard.settings.fingerselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Finger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FingerSelectionViewModel @Inject constructor(
    private val configManager: ConfigManager,
) : ViewModel() {

    val fingerSelections: LiveData<List<FingerSelectionItem>>
        get() = _fingerSelections
    private val _fingerSelections = MutableLiveData<List<FingerSelectionItem>>()

    fun start() {
        viewModelScope.launch {
            _fingerSelections.postValue(
                configManager.getDeviceConfiguration().fingersToCollect.toFingerSelectionItems()
            )
        }
    }

    private fun List<Finger>.toFingerSelectionItems(): List<FingerSelectionItem> {
        val result = mutableListOf<FingerSelectionItem>()
        this.forEach {
            if (result.lastOrNull()?.finger == it) {
                result.last().quantity++
            } else {
                result.add(FingerSelectionItem(it, 1))
            }
        }
        return result
    }
}

data class FingerSelectionItem(
    var finger: Finger,
    var quantity: Int
)

val ORDERED_FINGERS = listOf(
    Finger.LEFT_THUMB,
    Finger.LEFT_INDEX_FINGER,
    Finger.LEFT_3RD_FINGER,
    Finger.LEFT_4TH_FINGER,
    Finger.LEFT_5TH_FINGER,
    Finger.RIGHT_THUMB,
    Finger.RIGHT_INDEX_FINGER,
    Finger.RIGHT_3RD_FINGER,
    Finger.RIGHT_4TH_FINGER,
    Finger.RIGHT_5TH_FINGER
)
