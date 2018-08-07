package com.simprints.id.data.prefs.settings

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigPrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPrimitivePreference
import com.simprints.id.domain.Constants
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier


class SettingsPreferencesManagerImpl(prefs: ImprovedSharedPreferences,
                                     remoteConfig: FirebaseRemoteConfig,
                                     fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                                     groupSerializer: Serializer<Constants.GROUP>)
    : SettingsPreferencesManager {

    companion object {

        private const val NUDGE_MODE_KEY = "NudgeModeBool"
        private const val NUDGE_MODE_DEFAULT = true

        private const val CONSENT_KEY = "ConsentBool"
        private const val CONSENT_DEFAULT = true

        private const val QUALITY_THRESHOLD_KEY = "QualityThresholdInt"
        private const val QUALITY_THRESHOLD_DEFAULT = 60

        private const val NB_IDS_KEY = "NbOfIdsInt"
        private const val NB_IDS_DEFAULT = 10

        private const val LANGUAGE_KEY = "SelectedLanguage"
        private const val LANGUAGE_DEFAULT = ""

        private const val LANGUAGE_POSITION_KEY = "SelectedLanguagePosition"
        private const val LANGUAGE_POSITION_DEFAULT = 0

        private const val MATCHER_TYPE_KEY = "MatcherType"
        private const val MATCHER_TYPE_DEFAULT = 0

        private const val TIMEOUT_KEY = "TimeoutInt"
        private const val TIMEOUT_DEFAULT = 3

        private const val SYNC_GROUP_KEY = "SyncGroup"
        private val SYNC_GROUP_DEFAULT = Constants.GROUP.USER

        private const val MATCH_GROUP_KEY = "MatchGroup"
        private val MATCH_GROUP_DEFAULT = Constants.GROUP.MODULE

        private const val VIBRATE_KEY = "VibrateOn"
        private const val VIBRATE_DEFAULT = true

        private const val MATCHING_END_WAIT_TIME_KEY = "MatchingEndWaitTime"
        private const val MATCHING_END_WAIT_TIME_DEFAULT = 1

        private const val FINGER_STATUS_KEY = "FingerStatus"
        private val FINGER_STATUS_DEFAULT = mapOf(
            FingerIdentifier.RIGHT_THUMB to false,
            FingerIdentifier.RIGHT_INDEX_FINGER to false,
            FingerIdentifier.RIGHT_3RD_FINGER to false,
            FingerIdentifier.RIGHT_4TH_FINGER to false,
            FingerIdentifier.RIGHT_5TH_FINGER to false,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true,
            FingerIdentifier.LEFT_3RD_FINGER to false,
            FingerIdentifier.LEFT_4TH_FINGER to false,
            FingerIdentifier.LEFT_5TH_FINGER to false
        )

        private const val SYNC_ON_CALLOUT_KEY = "SyncOnCallout"
        private const val SYNC_ON_CALLOUT_DEFAULT = false
        private const val SYNC_ON_CALLOUT_ONLY_ON_WIFI_KEY = "SyncOnCalloutOnlyOnWifi"
        private const val SYNC_ON_CALLOUT_ONLY_ON_WIFI_DEFAULT = false
        private const val SYNC_ON_CALLOUT_ONLY_WHEN_CHARGING_KEY = "SyncOnCalloutOnlyWhenCharging"
        private const val SYNC_ON_CALLOUT_ONLY_WHEN_CHARGING_DEFAULT = false
        private const val SYNC_ON_CALLOUT_ONLY_WHEN_NOT_LOW_BATTERY_KEY = "SyncOnCalloutOnlyWhenNotLowBattery"
        private const val SYNC_ON_CALLOUT_ONLY_WHEN_NOT_LOW_BATTERY_DEFAULT = true

        private const val SCHEDULED_BACKGROUND_SYNC_KEY = "ScheduledBackgroundSync"
        private const val SCHEDULED_BACKGROUND_SYNC_DEFAULT = true
        private const val SCHEDULED_BACKGROUND_SYNC_ONLY_ON_WIFI_KEY = "ScheduledBackgroundSyncOnlyOnWifi"
        private const val SCHEDULED_BACKGROUND_SYNC_ONLY_ON_WIFI_DEFAULT = false
        private const val SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_CHARGING_KEY = "ScheduledBackgroundSyncOnlyWhenCharging"
        private const val SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_CHARGING_DEFAULT = false
        private const val SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_NOT_LOW_BATTERY_KEY = "ScheduledBackgroundSyncOnlyWhenNotLowBattery"
        private const val SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_NOT_LOW_BATTERY_DEFAULT = true
    }

    private val remoteConfigDefaults = mutableMapOf<String, Any>()

    // Should the UI automatically slide forward?
    override var nudgeMode: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, NUDGE_MODE_KEY, NUDGE_MODE_DEFAULT)

    // Has the CHW given consent to use Simprints ID?
    override var consent: Boolean
        by PrimitivePreference(prefs, CONSENT_KEY, CONSENT_DEFAULT)

    // Threshold that determines the UI feedback for a given fingerprint quality
    override var qualityThreshold: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, QUALITY_THRESHOLD_KEY, QUALITY_THRESHOLD_DEFAULT)

    // Number of GUIDs to be returned to the calling app as the result of an identification
    override var returnIdCount: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, NB_IDS_KEY, NB_IDS_DEFAULT)

    // Selected language
    override var language: String
        by OverridableRemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, LANGUAGE_KEY, LANGUAGE_DEFAULT)

    // Active language position to be displayed in the list
    override var languagePosition: Int
        by PrimitivePreference(prefs, LANGUAGE_POSITION_KEY, LANGUAGE_POSITION_DEFAULT)

    // Matcher type
    override var matcherType: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, MATCHER_TYPE_KEY, MATCHER_TYPE_DEFAULT)

    // Timeout seconds
    override var timeoutS: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, TIMEOUT_KEY, TIMEOUT_DEFAULT)

    // Sync group. Default is user
    override var syncGroup: Constants.GROUP
        by RemoteConfigComplexPreference(prefs, remoteConfig, remoteConfigDefaults, SYNC_GROUP_KEY, SYNC_GROUP_DEFAULT, groupSerializer)

    // Match group. Default is user
    override var matchGroup: Constants.GROUP
        by RemoteConfigComplexPreference(prefs, remoteConfig, remoteConfigDefaults, MATCH_GROUP_KEY, MATCH_GROUP_DEFAULT, groupSerializer)

    // Is the vibrate on
    override var vibrateMode: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, VIBRATE_KEY, VIBRATE_DEFAULT)

    // The number of seconds the screens pauses for when a match is complete
    override var matchingEndWaitTimeSeconds: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, MATCHING_END_WAIT_TIME_KEY, MATCHING_END_WAIT_TIME_DEFAULT)

    // The map of default fingers
    override var fingerStatus: Map<FingerIdentifier, Boolean>
        by RemoteConfigComplexPreference(prefs, remoteConfig, remoteConfigDefaults, FINGER_STATUS_KEY, FINGER_STATUS_DEFAULT, fingerIdToBooleanSerializer)

    override var syncOnCallout: Boolean
        by OverridableRemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SYNC_ON_CALLOUT_KEY, SYNC_ON_CALLOUT_DEFAULT)
    override var syncOnCalloutOnlyOnWifi: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SYNC_ON_CALLOUT_ONLY_ON_WIFI_KEY, SYNC_ON_CALLOUT_ONLY_ON_WIFI_DEFAULT)
    override var syncOnCalloutOnlyWhenCharging: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SYNC_ON_CALLOUT_ONLY_WHEN_CHARGING_KEY, SYNC_ON_CALLOUT_ONLY_WHEN_CHARGING_DEFAULT)
    override var syncOnCalloutOnlyWhenNotLowBattery: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SYNC_ON_CALLOUT_ONLY_WHEN_NOT_LOW_BATTERY_KEY, SYNC_ON_CALLOUT_ONLY_WHEN_NOT_LOW_BATTERY_DEFAULT)

    override var scheduledBackgroundSync: Boolean
        by OverridableRemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SCHEDULED_BACKGROUND_SYNC_KEY, SCHEDULED_BACKGROUND_SYNC_DEFAULT)
    override var scheduledBackgroundSyncOnlyOnWifi: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SCHEDULED_BACKGROUND_SYNC_ONLY_ON_WIFI_KEY, SCHEDULED_BACKGROUND_SYNC_ONLY_ON_WIFI_DEFAULT)
    override var scheduledBackgroundSyncOnlyWhenCharging: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_CHARGING_KEY, SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_CHARGING_DEFAULT)
    override var scheduledBackgroundSyncOnlyWhenNotLowBattery: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfig, remoteConfigDefaults, SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_NOT_LOW_BATTERY_KEY, SCHEDULED_BACKGROUND_SYNC_ONLY_WHEN_NOT_LOW_BATTERY_DEFAULT)

    init {
        remoteConfig.setDefaults(remoteConfigDefaults)
    }
}
