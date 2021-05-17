package com.simprints.id.activities.settings.fingerselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.unexpected.preferences.NoSuchPreferenceError
import timber.log.Timber

class FingerSelectionViewModel(private val preferencesManager: PreferencesManager,
                               private val crashReportManager: CrashReportManager) : ViewModel() {

    val items = MutableLiveData<List<FingerSelectionItem>>()

    private val _items: MutableList<FingerSelectionItem> = mutableListOf()

    private fun postUpdatedItems(block: MutableList<FingerSelectionItem>.() -> Unit) {
        _items.block()
        items.value = _items
    }

    fun start() {
        postUpdatedItems {
            addAll(determineFingerSelectionItemsFromPrefs())
        }
    }

    fun changeFingerSelection(itemIndex: Int, finger: FingerIdentifier) {
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
            }.firstOrNull() ?: FingerIdentifier.LEFT_THUMB
            add(FingerSelectionItem(fingerNotYetUsed, QUANTITY_OPTIONS.first(), true))
        }
    }

    fun resetFingerItems() {
        postUpdatedItems {
            clear()
            addAll(preferencesManager.getRemoteConfigFingerprintsToCollect().toFingerSelectionItems().apply {
                forEach { it.removable = false }
            })
        }
    }

    fun haveSettingsChanged() = determineFingerSelectionItemsFromPrefs() != _items.toList()

    fun canSavePreference(): Boolean {
        val highestNumberOfFingersInItems = _items.toFingerIdentifiers().groupingBy { it }.eachCount().values.max()
            ?: 1
        val maxAllowedFingers = QUANTITY_OPTIONS.max() ?: 1
        return highestNumberOfFingersInItems <= maxAllowedFingers
    }

    fun savePreference() {
        val fingerprintsToCollect = _items.toFingerIdentifiers()
        preferencesManager.fingerprintsToCollect = fingerprintsToCollect
        crashReportManager.setFingersSelectedCrashlyticsKey(fingerprintsToCollect)
    }

    private fun determineFingerSelectionItemsFromPrefs(): List<FingerSelectionItem> =
        preferencesManager.fingerprintsToCollect.toFingerSelectionItems().also { savedPref ->
            try {
                preferencesManager.getRemoteConfigFingerprintsToCollect().toFingerSelectionItems()
                    .map { it.finger }.distinct()
                    .forEach { finger ->
                        savedPref.firstOrNull { it.finger == finger }?.removable = false
                    }
            } catch (e: NoSuchPreferenceError) {
                Timber.e(e)
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

val ORDERED_FINGERS = listOf(
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
