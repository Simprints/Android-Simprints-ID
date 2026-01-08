package com.simprints.feature.dashboard.settings.fingerselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.infra.config.store.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FingerSelectionViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {
    val fingerSelections: LiveData<List<FingerSelectionSection>>
        get() = _fingerSelections
    private val _fingerSelections = MutableLiveData<List<FingerSelectionSection>>()

    fun start() {
        viewModelScope.launch {
            val fingerSelections = mutableListOf<FingerSelectionSection>()
            configRepository.getProjectConfiguration().fingerprint?.secugenSimMatcher?.fingersToCapture?.let {
                fingerSelections.add(FingerSelectionSection("SimMatcher", it.toFingerSelectionItems()))
            }
            configRepository.getProjectConfiguration().fingerprint?.nec?.fingersToCapture?.let {
                fingerSelections.add(FingerSelectionSection("NEC", it.toFingerSelectionItems()))
            }
            _fingerSelections.postValue(fingerSelections)
        }
    }

    private fun List<TemplateIdentifier>.toFingerSelectionItems(): List<FingerSelectionItem> {
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

data class FingerSelectionSection(
    val sdkName: String,
    val items: List<FingerSelectionItem>,
)

data class FingerSelectionItem(
    var finger: TemplateIdentifier,
    var quantity: Int,
)
