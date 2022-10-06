package com.simprints.id.data.prefs.settings

import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference

open class SettingsPreferencesManagerImpl(prefs: ImprovedSharedPreferences) :
    SettingsPreferencesManager {

    override var lastInstructionId: String by PrimitivePreference(
        prefs,
        LAST_INSTRUCTION_ID_KEY,
        LAST_INSTRUCTION_ID_DEFAULT
    )

    companion object {
        const val LAST_INSTRUCTION_ID_KEY = "LastInstructionId"
        const val LAST_INSTRUCTION_ID_DEFAULT = ""
    }

}
