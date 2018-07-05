package com.simprints.id.data.prefs.settings

import com.simprints.id.domain.Constants
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
    var autoSyncOnCallout: Boolean
    var syncGroup: Constants.GROUP
    var matchGroup: Constants.GROUP
    var vibrateMode: Boolean
    var matchingEndWaitTimeSeconds: Int
    var fingerStatusPersist: Boolean
    var fingerStatus: Map<FingerIdentifier, Boolean>

}
