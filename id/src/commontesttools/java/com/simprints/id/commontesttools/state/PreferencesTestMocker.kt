package com.simprints.id.commontesttools.state

import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.testtools.common.syntax.whenever


fun mockSettingsPreferencesManager(settingsPreferencesManager: SettingsPreferencesManager,
                                   language: String = "en",
                                   programName: String = "PROGRAM_NAME",
                                   organizationName: String = "ORGANIZATION") {

    whenever(settingsPreferencesManager.language).thenReturn(language)
    whenever(settingsPreferencesManager.programName).thenReturn(programName)
    whenever(settingsPreferencesManager.organizationName).thenReturn(organizationName)
}
