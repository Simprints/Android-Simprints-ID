package com.simprints.id.shared

import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.testframework.common.syntax.whenever


fun mockSettingsPreferencesManager(settingsPreferencesManager: SettingsPreferencesManager,
                                   parentalConsentExists: Boolean = false,
                                   generalConsentOptions: String = "",
                                   parentalConsentOptions: String = "",
                                   language: String = "en",
                                   programName: String = "PROGRAM_NAME",
                                   organizationName: String = "ORGANIZATION") {

    whenever(settingsPreferencesManager.language).thenReturn(language)
    whenever(settingsPreferencesManager.programName).thenReturn(programName)
    whenever(settingsPreferencesManager.organizationName).thenReturn(organizationName)

    whenever(settingsPreferencesManager.parentalConsentExists).thenReturn(parentalConsentExists)
    whenever(settingsPreferencesManager.generalConsentOptionsJson).thenReturn(generalConsentOptions)
    whenever(settingsPreferencesManager.parentalConsentOptionsJson).thenReturn(parentalConsentOptions)
}
