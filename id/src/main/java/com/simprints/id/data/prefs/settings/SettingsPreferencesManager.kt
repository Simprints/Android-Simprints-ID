package com.simprints.id.data.prefs.settings

import com.simprints.id.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier


interface SettingsPreferencesManager {

    var nudgeMode: Boolean
    var consent: Boolean
    var qualityThreshold: Int
    var returnIdCount: Int
    var language: String
    var languagePosition: Int
    var matcherType: Int
    var timeoutS: Int
    var syncGroup: com.simprints.id.libdata.tools.Constants.GROUP
    var matchGroup: com.simprints.id.libdata.tools.Constants.GROUP
    var vibrateMode: Boolean
    var matchingEndWaitTimeSeconds: Int
    var fingerStatusPersist: Boolean
    var fingerStatus: Map<FingerIdentifier, Boolean>

}
