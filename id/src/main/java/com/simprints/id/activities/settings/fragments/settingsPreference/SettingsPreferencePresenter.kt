package com.simprints.id.activities.settings.fragments.settingsPreference

import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import com.simprints.id.FingerIdentifier
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.GROUP
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import javax.inject.Inject


class SettingsPreferencePresenter(private val view: SettingsPreferenceContract.View,
                                  component: AppComponent) :
    SettingsPreferenceContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper

    init {
        component.inject(this)
    }

    override fun start() {
        configureSelectModulePreference()
        configureAvailableLanguageEntriesFromProjectLanguages()
        loadPreferenceValuesAndBindThemToChangeListeners()
    }

    private fun configureSelectModulePreference() {
        clearSelectModulePreferenceOfOldValues()
        configureVisibilityOfSelectModulePreference()
        configureSelectModuleEntriesFromModuleIdOptions()
    }

    private fun clearSelectModulePreferenceOfOldValues() {
        preferencesManager.selectedModules = preferencesManager.selectedModules.filter {
            preferencesManager.moduleIdOptions.contains(it)
        }.toSet()
    }

    private fun configureVisibilityOfSelectModulePreference() {
        val isModuleListNonEmpty = preferencesManager.moduleIdOptions.isNotEmpty()
        val isModuleSync = preferencesManager.syncGroup == GROUP.MODULE

        view.setSelectModulePreferenceEnabled(isModuleSync and isModuleListNonEmpty) // TODO : log in analytics if XOR these conditions is true
    }

    private fun configureSelectModuleEntriesFromModuleIdOptions() {
        val preference = view.getPreferenceForSelectModules() as MultiSelectListPreference

        preference.entries = preferencesManager.moduleIdOptions.toTypedArray()
        preference.entryValues = preferencesManager.moduleIdOptions.toTypedArray()
    }

    private fun configureAvailableLanguageEntriesFromProjectLanguages() {
        val availableLanguages = preferencesManager.projectLanguages
        val languageCodeToName = view.getLanguageCodeAndNamePairs()

        val (availableLanguageNames, availableLanguageCodes) = computeAvailableLanguageNamesAndCodes(availableLanguages, languageCodeToName)

        val preference = view.getPreferenceForLanguage() as ListPreference
        preference.entries = availableLanguageNames.toTypedArray()
        preference.entryValues = availableLanguageCodes.toTypedArray()
    }

    private fun computeAvailableLanguageNamesAndCodes(availableLanguages: Array<String>, languageCodeToName: Map<String, String>): Pair<MutableList<String>, MutableList<String>> {
        val availableLanguageNames = mutableListOf<String>()
        val availableLanguageCodes = mutableListOf<String>()
        availableLanguages.forEach { code ->
            val name = languageCodeToName[code]
            if (name != null) {
                availableLanguageNames.add(name)
                availableLanguageCodes.add(code)
            }
        }

        if (availableLanguageNames.isEmpty()) {
            availableLanguageNames.addAll(languageCodeToName.values)
            availableLanguageCodes.addAll(languageCodeToName.keys)
        }
        return Pair(availableLanguageNames, availableLanguageCodes)
    }

    private fun loadPreferenceValuesAndBindThemToChangeListeners() {
        loadValueAndBindChangeListener(view.getPreferenceForLanguage())
        loadValueAndBindChangeListener(view.getPreferenceForSelectModules())
        loadValueAndBindChangeListener(view.getPreferenceForDefaultFingers())
        loadValueAndBindChangeListener(view.getPreferenceForAbout())
    }

    internal fun loadValueAndBindChangeListener(preference: Preference) {
        when (preference.key) {
            view.getKeyForLanguagePreference() -> {
                loadLanguagePreference(preference as ListPreference)
                preference.setChangeListener { value: String -> handleLanguagePreferenceChanged(preference, value) }
            }
            view.getKeyForSelectModulesPreference() -> {
                loadSelectModulesPreference(preference as MultiSelectListPreference)
                preference.setChangeListener { value: HashSet<String> -> handleSelectModulesChanged(value) }
            }
            view.getKeyForDefaultFingersPreference() -> {
                loadDefaultFingersPreference(preference as MultiSelectListPreference)
                preference.setChangeListener { value: HashSet<String> -> handleDefaultFingersChanged(preference, value) }
            }
            view.getKeyForAboutPreference() -> {
                preference.setOnPreferenceClickListener {
                    view.openSettingAboutActivity()
                    true
                }
            }
        }
    }

    private inline fun <reified V : Any> Preference.setChangeListener(crossinline listener: (V) -> Unit) {
        onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
            listener(value as V)
            true
        }
    }

    internal fun loadLanguagePreference(preference: ListPreference) {
        preference.value = preferencesManager.language
        val index = preference.findIndexOfValue(preference.value)
        preference.summary = if (index >= 0) {
            preference.entries[index]
        } else {
            null
        }
    }

    internal fun loadSelectModulesPreference(preference: MultiSelectListPreference) {
        preference.values = preferencesManager.selectedModules
    }

    internal fun loadDefaultFingersPreference(preference: MultiSelectListPreference) {
        preference.values = getHashSetFromFingersMap(preferencesManager.fingerStatus).toHashSet()
    }

    private fun handleLanguagePreferenceChanged(listPreference: ListPreference, stringValue: String): Boolean {
        val index = listPreference.findIndexOfValue(stringValue)
        preferencesManager.language = stringValue

        listPreference.summary = if (index >= 0) {
            listPreference.entries[index]
        } else {
            null
        }
        return true
    }

    private fun handleSelectModulesChanged(moduleIdHash: HashSet<String>): Boolean {
        when {
            moduleIdHash.size == 0 -> handleNoModulesSelected(moduleIdHash)
            moduleIdHash.size > MAX_SELECTED_MODULES -> handleTooManyModulesSelected(moduleIdHash)
            else -> preferencesManager.selectedModules = moduleIdHash
        }
        return true
    }

    private fun handleNoModulesSelected(moduleIdHash: HashSet<String>) {
        view.showToastForNoModulesSelected()
        moduleIdHash.clear()
        moduleIdHash.addAll(preferencesManager.selectedModules)
    }

    private fun handleTooManyModulesSelected(moduleIdHash: HashSet<String>) {
        view.showToastForTooManyModulesSelected(MAX_SELECTED_MODULES)
        moduleIdHash.clear()
        moduleIdHash.addAll(preferencesManager.selectedModules)
    }

    private fun handleDefaultFingersChanged(preference: MultiSelectListPreference,
                                            fingersHash: HashSet<String>): Boolean {
        if (selectionContainsDefaultFingers(fingersHash)) {
            preferencesManager.fingerStatus = getMapFromFingersHash(fingersHash)
        } else {
            view.showToastForInvalidSelectionOfFingers()
            fingersHash.clear()
            fingersHash.addAll(preference.values)
        }
        return true
    }

    private fun selectionContainsDefaultFingers(fingersHash: HashSet<String>): Boolean =
        fingersHash.containsAll(getHashSetFromFingersMap(preferencesManager.getRemoteConfigFingerStatus()))

    private fun getHashSetFromFingersMap(fingersMap: Map<FingerIdentifier, Boolean>) =
        fingersMap.filter { it.value }.keys.map { it.toString() }.toHashSet()

    private fun getMapFromFingersHash(fingersHash: HashSet<String>): Map<FingerIdentifier, Boolean> =
        mutableMapOf<FingerIdentifier, Boolean>().apply {
            fingersHash.map { FingerIdentifier.valueOf(it) }.forEach { this[it] = true }
        }

    companion object {
        const val MAX_SELECTED_MODULES = 6
    }
}
