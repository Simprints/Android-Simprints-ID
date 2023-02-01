package com.simprints.feature.dashboard.settings.fingerselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.ExternalScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.FINGERS_SELECTED
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FingerSelectionViewModel @Inject constructor(
    private val configManager: ConfigManager,
    @ExternalScope private val externalScope: CoroutineScope,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val fingerSelections: LiveData<List<FingerSelectionItem>>
        get() = _fingerSelections
    private val _fingerSelections = MutableLiveData<List<FingerSelectionItem>>()

    private val _items: MutableList<FingerSelectionItem> = mutableListOf()
    private var initialItems: List<FingerSelectionItem> = listOf()

    private fun postUpdatedItems(block: suspend MutableList<FingerSelectionItem>.() -> Unit) {
        viewModelScope.launch(dispatcher) {
            _items.block()
            _fingerSelections.postValue(_items)
        }
    }

    fun start() {
        postUpdatedItems {
            initialItems = determineFingerSelectionItemsFromPrefs()
            addAll(initialItems)
        }
    }

    fun changeFingerSelection(itemIndex: Int, finger: Finger) {
        _items[itemIndex].finger = finger
    }

    fun changeQuantitySelection(itemIndex: Int, quantity: Int) {
        _items[itemIndex].quantity = quantity
    }

    fun moveItem(from: Int, to: Int) {
        _items.add(to, _items.removeAt(from))
    }

    fun removeItem(itemIndex: Int) {
        postUpdatedItems {
            removeAt(itemIndex)
        }
    }

    fun addNewFinger() {
        postUpdatedItems {
            val fingerNotYetUsed = ORDERED_FINGERS.toMutableList().apply {
                removeAll(this@postUpdatedItems.map { it.finger })
            }.firstOrNull() ?: Finger.LEFT_THUMB
            add(FingerSelectionItem(fingerNotYetUsed, QUANTITY_OPTIONS.first(), true))
        }
    }

    fun resetFingerItems() {
        postUpdatedItems {
            clear()
            addAll(
                configManager
                    .getProjectConfiguration()
                    .fingerprint!! // It will not be null as we have already checked that the modality Fingerprint is enabled
                    .fingersToCapture
                    .toFingerSelectionItems()
                    .onEach { it.removable = false }
            )
        }
    }

    fun hasSelectionChanged() = initialItems != _items.toList()

    fun savePreference() {
        externalScope.launch {
            val fingerprintsToCollect = _items.toFingerIdentifiers()
            configManager.updateDeviceConfiguration {
                it.apply {
                    it.fingersToCollect = fingerprintsToCollect
                }
            }
            initialItems = _items
            Simber.tag(FINGERS_SELECTED, true).i(fingerprintsToCollect.map { it.name }.toString())
        }
    }

    private suspend fun determineFingerSelectionItemsFromPrefs(): List<FingerSelectionItem> =
        configManager.getDeviceConfiguration().fingersToCollect.toFingerSelectionItems()
            .also { savedPref ->
                configManager
                    .getProjectConfiguration()
                    .fingerprint!!
                    .fingersToCapture
                    .toFingerSelectionItems()
                    .map { it.finger }.distinct()
                    .forEach { finger ->
                        savedPref.firstOrNull { it.finger == finger }?.removable = false
                    }
            }

    private fun List<Finger>.toFingerSelectionItems(): List<FingerSelectionItem> {
        val result = mutableListOf<FingerSelectionItem>()
        this.forEach {
            if (result.lastOrNull()?.finger == it) {
                result.last().quantity++
            } else {
                result.add(FingerSelectionItem(it, 1, true))
            }
        }
        return result
    }

    private fun List<FingerSelectionItem>.toFingerIdentifiers(): List<Finger> =
        flatMap { item ->
            List(item.quantity) { item.finger }
        }
}

data class FingerSelectionItem(
    var finger: Finger,
    var quantity: Int,
    var removable: Boolean
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

val QUANTITY_OPTIONS = arrayOf(1, 2, 3, 4, 5)
