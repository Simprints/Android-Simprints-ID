package com.simprints.id.data.prefs.settings

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.delegates.ComplexPreference
import com.simprints.id.tools.delegates.PrimitivePreference
import com.simprints.id.tools.serializers.Serializer
import com.simprints.id.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier


class SettingsPreferencesManagerImpl(prefs: ImprovedSharedPreferences,
                                     fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                                     groupSerializer: Serializer<com.simprints.id.libdata.tools.Constants.GROUP>)
    : SettingsPreferencesManager {

    companion object {

        private val NUDGE_MODE_KEY = "NudgeModeBool"
        private val NUDGE_MODE_DEFAULT = true

        private val CONSENT_KEY = "ConsentBool"
        private val CONSENT_DEFAULT = true

        private val QUALITY_THRESHOLD_KEY = "QualityThresholdInt"
        private val QUALITY_THRESHOLD_DEFAULT = 60

        private val NB_IDS_KEY = "NbOfIdsInt"
        private val NB_IDS_DEFAULT = 10

        private val LANGUAGE_KEY = "SelectedLanguage"
        private val LANGUAGE_DEFAULT = ""

        private val LANGUAGE_POSITION_KEY = "SelectedLanguagePosition"
        private val LANGUAGE_POSITION_DEFAULT = 0

        private val MATCHER_TYPE_KEY = "MatcherType"
        private val MATCHER_TYPE_DEFAULT = 0

        private val TIMEOUT_KEY = "TimeoutInt"
        private val TIMEOUT_DEFAULT = 3

        private val SYNC_GROUP_KEY = "SyncGroup"
        private val SYNC_GROUP_DEFAULT = com.simprints.id.libdata.tools.Constants.GROUP.USER

        private val MATCH_GROUP_KEY = "MatchGroup"
        private val MATCH_GROUP_DEFAULT = com.simprints.id.libdata.tools.Constants.GROUP.USER

        private val VIBRATE_KEY = "VibrateOn"
        private val VIBRATE_DEFAULT = true

        private val MATCHING_END_WAIT_TIME_KEY = "MatchingEndWaitTime"
        private val MATCHING_END_WAIT_TIME_DEFAULT = 1

        private val PERSIST_FINGER_KEY = "PersistFingerStatus"
        private val PERSIST_FINGER_DEFAULT = false

        private val FINGER_STATUS_KEY = "FingerStatus"
        private val FINGER_STATUS_DEFAULT =  FingerIdentifier.values()
            .map { Pair(it, false) }
            .toMap()

    }

    // Should the UI automatically slide forward?
    override var nudgeMode: Boolean
        by PrimitivePreference(prefs, NUDGE_MODE_KEY, NUDGE_MODE_DEFAULT)

    // Has the CHW given consent to use Simprints ID?
    override var consent: Boolean
        by PrimitivePreference(prefs, CONSENT_KEY, CONSENT_DEFAULT)

    // Threshold that determines the UI feedback for a given fingerprint quality
    override var qualityThreshold: Int
        by PrimitivePreference(prefs, QUALITY_THRESHOLD_KEY, QUALITY_THRESHOLD_DEFAULT)

    // Number of GUIDs to be returned to the calling app as the result of an identification
    override var returnIdCount: Int
        by PrimitivePreference(prefs, NB_IDS_KEY, NB_IDS_DEFAULT)

    // Selected language
    override var language: String
        by PrimitivePreference(prefs, LANGUAGE_KEY, LANGUAGE_DEFAULT)

    // Active language position to be displayed in the list
    override var languagePosition: Int
        by PrimitivePreference(prefs, LANGUAGE_POSITION_KEY, LANGUAGE_POSITION_DEFAULT)

    // Matcher type
    override var matcherType: Int
        by PrimitivePreference(prefs, MATCHER_TYPE_KEY, MATCHER_TYPE_DEFAULT)

    // Timeout seconds
    override var timeoutS: Int
        by PrimitivePreference(prefs, TIMEOUT_KEY, TIMEOUT_DEFAULT)

    // Sync group. Default is user
    override var syncGroup: com.simprints.id.libdata.tools.Constants.GROUP
        by ComplexPreference(prefs, SYNC_GROUP_KEY, SYNC_GROUP_DEFAULT, groupSerializer)

    // Match group. Default is user
    override var matchGroup: com.simprints.id.libdata.tools.Constants.GROUP
        by ComplexPreference(prefs, MATCH_GROUP_KEY, MATCH_GROUP_DEFAULT, groupSerializer)

    // Is the vibrate on
    override var vibrateMode: Boolean
        by PrimitivePreference(prefs, VIBRATE_KEY, VIBRATE_DEFAULT)

    // TODO: document that
    override var matchingEndWaitTimeSeconds: Int
        by PrimitivePreference(prefs, MATCHING_END_WAIT_TIME_KEY, MATCHING_END_WAIT_TIME_DEFAULT)

    // True if the fingers status should be persisted, false else
    override var fingerStatusPersist: Boolean
        by PrimitivePreference(prefs, PERSIST_FINGER_KEY, PERSIST_FINGER_DEFAULT)

    override var fingerStatus: Map<FingerIdentifier, Boolean>
        by ComplexPreference(prefs, FINGER_STATUS_KEY, FINGER_STATUS_DEFAULT, fingerIdToBooleanSerializer)

}
