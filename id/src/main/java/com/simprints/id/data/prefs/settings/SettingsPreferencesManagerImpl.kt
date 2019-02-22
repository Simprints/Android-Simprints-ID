package com.simprints.id.data.prefs.settings

import com.google.gson.JsonSyntaxException
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.ComplexPreference
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigPrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPrimitivePreference
import com.simprints.id.domain.Constants
import com.simprints.id.domain.consent.GeneralConsent
import com.simprints.id.domain.consent.ParentalConsent
import com.simprints.id.exceptions.unsafe.preferences.NoSuchPreferenceError
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier


open class SettingsPreferencesManagerImpl(prefs: ImprovedSharedPreferences,
                                          private val remoteConfigWrapper: RemoteConfigWrapper,
                                          private val fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                                          groupSerializer: Serializer<Constants.GROUP>,
                                          languagesStringArraySerializer: Serializer<Array<String>>,
                                          moduleIdOptionsStringSetSerializer: Serializer<Set<String>>,
                                          peopleDownSyncTriggerToSerializer: Serializer<Map<PeopleDownSyncTrigger, Boolean>>)
    : SettingsPreferencesManager {

    companion object {

        const val NUDGE_MODE_KEY = "NudgeModeBool"
        const val NUDGE_MODE_DEFAULT = true

        const val CONSENT_KEY = "ConsentBool"
        const val CONSENT_DEFAULT = true

        const val QUALITY_THRESHOLD_KEY = "QualityThresholdInt"
        const val QUALITY_THRESHOLD_DEFAULT = 60

        const val NB_IDS_KEY = "NbOfIdsInt"
        const val NB_IDS_DEFAULT = 10

        const val PROJECT_LANGUAGES_POSITION_KEY = "ProjectLanguages"
        val PROJECT_LANGUAGES_POSITION_DEFAULT = arrayOf<String>()

        const val LANGUAGE_KEY = "SelectedLanguage"
        const val LANGUAGE_DEFAULT = "en"

        const val LANGUAGE_POSITION_KEY = "SelectedLanguagePosition"
        const val LANGUAGE_POSITION_DEFAULT = 0

        const val MATCHER_TYPE_KEY = "MatcherType"
        const val MATCHER_TYPE_DEFAULT = 0

        const val TIMEOUT_KEY = "TimeoutInt"
        const val TIMEOUT_DEFAULT = 3

        const val MODULE_ID_OPTIONS_KEY = "ModuleIdOptions"
        val MODULE_ID_OPTIONS_DEFAULT = setOf<String>()

        const val SELECTED_MODULES_KEY = "SelectedModules"
        val SELECTED_MODULES_DEFAULT = setOf<String>()

        const val SYNC_GROUP_KEY = "SyncGroup"
        val SYNC_GROUP_DEFAULT = Constants.GROUP.USER

        const val MATCH_GROUP_KEY = "MatchGroup"
        val MATCH_GROUP_DEFAULT = Constants.GROUP.USER

        const val VIBRATE_KEY = "VibrateOn"
        const val VIBRATE_DEFAULT = true

        const val MATCHING_END_WAIT_TIME_KEY = "MatchingEndWaitTime"
        const val MATCHING_END_WAIT_TIME_DEFAULT = 1

        const val FINGER_STATUS_KEY = "FingerStatus"
        val FINGER_STATUS_DEFAULT = mapOf(
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

        const val PROGRAM_NAME_KEY = "ProgramName"
        const val PROGRAM_NAME_DEFAULT = "this program"

        const val ORGANIZATION_NAME_KEY = "OrganizationName"
        const val ORGANIZATION_NAME_DEFAULT = "This organization"

        const val LOGO_EXISTS_KEY = "LogoExists"
        const val LOGO_EXISTS_DEFAULT = true

        const val PARENTAL_CONSENT_EXISTS_KEY = "ConsentParentalExists"
        const val PARENTAL_CONSENT_EXISTS_DEFAULT = false

        const val GENERAL_CONSENT_OPTIONS_JSON_KEY = "ConsentGeneralOptions"
        val GENERAL_CONSENT_OPTIONS_JSON_DEFAULT: String = JsonHelper.toJson(GeneralConsent())

        const val PARENTAL_CONSENT_OPTIONS_JSON_KEY = "ConsentParentalOptions"
        val PARENTAL_CONSENT_OPTIONS_JSON_DEFAULT: String = JsonHelper.toJson(ParentalConsent())

        const val PEOPLE_DOWN_SYNC_TRIGGERS_KEY = "PeopleDownSyncTriggers"
        val PEOPLE_DOWN_SYNC_TRIGGERS_DEFAULT = mapOf(
            PeopleDownSyncTrigger.MANUAL to true,
            PeopleDownSyncTrigger.PERIODIC_BACKGROUND to true,
            PeopleDownSyncTrigger.ON_LAUNCH_CALLOUT to false
        )
    }

    // Should the UI automatically slide forward?
    override var nudgeMode: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, NUDGE_MODE_KEY, NUDGE_MODE_DEFAULT)

    // Has the CHW given consent to use Simprints ID?
    override var consent: Boolean
        by PrimitivePreference(prefs, CONSENT_KEY, CONSENT_DEFAULT)

    // Threshold that determines the UI feedback for a given fingerprint quality
    override var qualityThreshold: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, QUALITY_THRESHOLD_KEY, QUALITY_THRESHOLD_DEFAULT)

    // Number of GUIDs to be returned to the calling app as the result of an identification
    override var returnIdCount: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, NB_IDS_KEY, NB_IDS_DEFAULT)

    // What languages should be selectable for this project. Serialize as commas separated list. The empty list defaults to all languages
    override var projectLanguages: Array<String>
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, PROJECT_LANGUAGES_POSITION_KEY, PROJECT_LANGUAGES_POSITION_DEFAULT, languagesStringArraySerializer)

    // Selected language
    override var language: String
        by OverridableRemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, LANGUAGE_KEY, LANGUAGE_DEFAULT)

    // Active language position to be displayed in the list
    override var languagePosition: Int
        by PrimitivePreference(prefs, LANGUAGE_POSITION_KEY, LANGUAGE_POSITION_DEFAULT)

    // Matcher type
    override var matcherType: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, MATCHER_TYPE_KEY, MATCHER_TYPE_DEFAULT)

    // Timeout seconds
    override var timeoutS: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, TIMEOUT_KEY, TIMEOUT_DEFAULT)

    // What modules will be available to sync by for this project. Serialize as pipe (|) separated list. Empty list indicates that module sync should not be possible.
    override var moduleIdOptions: Set<String>
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, MODULE_ID_OPTIONS_KEY, MODULE_ID_OPTIONS_DEFAULT, moduleIdOptionsStringSetSerializer)

    // What modules were selected by the user
    override var selectedModules: Set<String>
        by ComplexPreference(prefs, SELECTED_MODULES_KEY, SELECTED_MODULES_DEFAULT, moduleIdOptionsStringSetSerializer)

    // Sync group. Default is user
    override var syncGroup: Constants.GROUP
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, SYNC_GROUP_KEY, SYNC_GROUP_DEFAULT, groupSerializer)

    // Match group. Default is user
    override var matchGroup: Constants.GROUP
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, MATCH_GROUP_KEY, MATCH_GROUP_DEFAULT, groupSerializer)

    // Is the vibrate on
    override var vibrateMode: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, VIBRATE_KEY, VIBRATE_DEFAULT)

    // The number of seconds the screens pauses for when a match is complete
    override var matchingEndWaitTimeSeconds: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, MATCHING_END_WAIT_TIME_KEY, MATCHING_END_WAIT_TIME_DEFAULT)

    // The map of default fingers
    /** @throws JsonSyntaxException */
    override var fingerStatus: Map<FingerIdentifier, Boolean>
        by OverridableRemoteConfigComplexPreference(prefs, remoteConfigWrapper, FINGER_STATUS_KEY, FINGER_STATUS_DEFAULT, fingerIdToBooleanSerializer)

    // Name of the partner's program
    override var programName: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, PROGRAM_NAME_KEY, PROGRAM_NAME_DEFAULT)
    // Name of the partner's organization
    override var organizationName: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, ORGANIZATION_NAME_KEY, ORGANIZATION_NAME_DEFAULT)

    // Whether to show the Simprints logo at the top of the launch activity
    override var logoExists: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, LOGO_EXISTS_KEY, LOGO_EXISTS_DEFAULT)

    // Whether the parental consent should be shown
    override var parentalConsentExists: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, PARENTAL_CONSENT_EXISTS_KEY, PARENTAL_CONSENT_EXISTS_DEFAULT)
    // The options of the general consent as a JSON string of booleans
    override var generalConsentOptionsJson: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, GENERAL_CONSENT_OPTIONS_JSON_KEY, GENERAL_CONSENT_OPTIONS_JSON_DEFAULT)
    // The options of the parental consent as a JSON string of booleans
    override var parentalConsentOptionsJson: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, PARENTAL_CONSENT_OPTIONS_JSON_KEY, PARENTAL_CONSENT_OPTIONS_JSON_DEFAULT)

    override var peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, PEOPLE_DOWN_SYNC_TRIGGERS_KEY, PEOPLE_DOWN_SYNC_TRIGGERS_DEFAULT, peopleDownSyncTriggerToSerializer)

    init {
        remoteConfigWrapper.registerAllPreparedDefaultValues()
    }

    override fun getRemoteConfigStringPreference(key: String) = remoteConfigWrapper.getString(key)
        ?: throw NoSuchPreferenceError.forKey(key)

    override fun <T : Any> getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T = serializer.deserialize(getRemoteConfigStringPreference(key))

    override fun getRemoteConfigFingerStatus() = getRemoteConfigComplexPreference(FINGER_STATUS_KEY, fingerIdToBooleanSerializer)
}
