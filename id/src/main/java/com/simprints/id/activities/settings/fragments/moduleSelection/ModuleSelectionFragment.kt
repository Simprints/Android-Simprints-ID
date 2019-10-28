package com.simprints.id.activities.settings.fragments.moduleSelection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simprints.id.R
import kotlinx.android.synthetic.main.fragment_module_selection.*

class ModuleSelectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_module_selection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView.queryHint = getString(R.string.hint_search_modules)
    }

    /*private fun configureSelectModuleEntriesFromModuleIdOptions() {
        val preference = view.getPreferenceForSelectModules() as MultiSelectListPreference

        preference.entries = preferencesManager.moduleIdOptions.toTypedArray()
        preference.entryValues = preferencesManager.moduleIdOptions.toTypedArray()
    }

    private fun handleSelectModulesChanged(moduleIdHash: HashSet<String>): Boolean {
        when {
            moduleIdHash.size == 0 -> { handleNoModulesSelected(moduleIdHash) }
            moduleIdHash.size > MAX_SELECTED_MODULES -> { handleTooManyModulesSelected(moduleIdHash) }
            else -> {
                preferencesManager.selectedModules = moduleIdHash
                logMessageForCrashReport("Modules set to ${preferencesManager.selectedModules}")
                setCrashlyticsKeyForModules()
            }
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

    private fun setCrashlyticsKeyForModules() {
        crashReportManager.setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.SETTINGS, CrashReportTrigger.UI, message = message)
    }

    companion object {
        const val MAX_SELECTED_MODULES = 6
    }*/

}
