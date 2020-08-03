package com.simprints.id.activities.settings.fingerselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.PreferencesManager

class FingerSelectionViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    val items = MutableLiveData<List<FingerSelectionItem>>()

    private val _items: MutableList<FingerSelectionItem> = mutableListOf()

    private fun postUpdatedItems(block: MutableList<FingerSelectionItem>.() -> Unit) {
        _items.block()
        items.postValue(_items)
    }

    fun start() {
        postUpdatedItems {
            addAll(determineFingerSelectionItemsFromPrefs())
        }
    }

    fun changeFingerSelection(itemIndex: Int, fingerIndex: Int) {
        _items[itemIndex].finger = orderedFingers()[fingerIndex]
    }

    fun changeQuantitySelection(itemIndex: Int, quantity: Int) {
        _items[itemIndex].quantity = QUANTITY_OPTIONS[quantity]
    }

    fun removeItem(itemIndex: Int) {
        postUpdatedItems {
            removeAt(itemIndex)
        }
    }

    fun addNewFinger() {
        postUpdatedItems {
            val fingerNotYetUsed = orderedFingers().toMutableList().apply {
                removeAll(this@postUpdatedItems.map { it.finger })
            }.firstOrNull() ?: FingerIdentifier.LEFT_THUMB
            add(FingerSelectionItem(fingerNotYetUsed, QUANTITY_OPTIONS.first(), true))
        }
    }

    fun resetFingerItems() {
        postUpdatedItems {
            clear()
            addAll(determineFingerSelectionItemsFromPrefs())
        }
    }

    fun haveSettingsChanged() = determineFingerSelectionItemsFromPrefs() != _items.toList()

    fun savePreference() {
        preferencesManager.fingerprintsToCollect = _items.toFingerIdentifiers()
    }

    private fun determineFingerSelectionItemsFromPrefs(): List<FingerSelectionItem> =
        preferencesManager.fingerprintsToCollect.toFingerSelectionItems().also { savedPref ->
            preferencesManager.getRemoteConfigFingerprintsToCollect().toFingerSelectionItems()
                .map { it.finger }.distinct()
                .forEach { finger -> savedPref.firstOrNull { it.finger == finger }?.removable = false
            }
        }

    private fun List<FingerIdentifier>.toFingerSelectionItems(): List<FingerSelectionItem> {
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

    private fun List<FingerSelectionItem>.toFingerIdentifiers(): List<FingerIdentifier> =
        flatMap { item ->
            List(item.quantity) { item.finger }
        }
}

data class FingerSelectionItem(var finger: FingerIdentifier, var quantity: Int, var removable: Boolean)

fun orderedFingers() = listOf(
    FingerIdentifier.LEFT_THUMB,
    FingerIdentifier.LEFT_INDEX_FINGER,
    FingerIdentifier.LEFT_3RD_FINGER,
    FingerIdentifier.LEFT_4TH_FINGER,
    FingerIdentifier.LEFT_5TH_FINGER,
    FingerIdentifier.RIGHT_THUMB,
    FingerIdentifier.RIGHT_INDEX_FINGER,
    FingerIdentifier.RIGHT_3RD_FINGER,
    FingerIdentifier.RIGHT_4TH_FINGER,
    FingerIdentifier.RIGHT_5TH_FINGER
)

val QUANTITY_OPTIONS = arrayOf(1, 2, 3, 4, 5)
