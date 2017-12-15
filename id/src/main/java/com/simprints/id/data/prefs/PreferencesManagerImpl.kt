package com.simprints.id.data.prefs

import android.content.Context
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameters.MainCalloutParameters
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.delegates.ComplexPreference
import com.simprints.id.tools.delegates.PrimitivePreference
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier


class PreferencesManagerImpl(prefs: ImprovedSharedPreferences,
                             fingerIdentifierToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                             calloutTypeSerializer: Serializer<CalloutType>,
                             groupSerializer: Serializer<Constants.GROUP>): PreferencesManager {

    companion object {

        val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        val PREF_MODE = Context.MODE_PRIVATE

        // Session state
        private val CALLOUT_KEY = "CalloutType"
        private val CALLOUT_DEFAULT = CalloutType.INVALID_OR_MISSING

        private val MODULE_ID_KEY = "ModuleId"
        private val MODULE_ID_DEFAULT = ""

        private val USER_ID_KEY = "UserId"
        private val USER_ID_DEFAULT = ""

        private val PATIENT_ID_KEY = "PatientId"
        private val PATIENT_ID_DEFAULT = ""

        private val CALLING_PACKAGE_KEY = "CallingPackage"
        private val CALLING_PACKAGE_DEFAULT = ""

        private val METADATA_KEY = "Metadata"
        private val METADATA_DEFAULT = ""

        private val RESULT_FORMAT_KEY = "ResultFormat"
        private val RESULT_FORMAT_DEFAULT = ""

        private val SESSION_ID_KEY = "SessionId"
        private val SESSION_ID_DEFAULT = ""

        // Settings

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

        private val APP_KEY_KEY = "AppKey"
        private val APP_KEY_DEFAULT = ""

        private val SYNC_GROUP_KEY = "SyncGroup"
        private val SYNC_GROUP_DEFAULT = Constants.GROUP.USER

        private val MATCH_GROUP_KEY = "MatchGroup"
        private val MATCH_GROUP_DEFAULT = Constants.GROUP.USER

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

    // CalloutType of the current session
    override var calloutType: CalloutType by ComplexPreference(prefs, CALLOUT_KEY, CALLOUT_DEFAULT, calloutTypeSerializer)

    // Module ID of the current session
    override var moduleId: String by PrimitivePreference(prefs, MODULE_ID_KEY, MODULE_ID_DEFAULT)

    // User ID of the current session
    override var userId: String by PrimitivePreference(prefs, USER_ID_KEY, USER_ID_DEFAULT)

    // Patient ID of the current session
    override var patientId: String by PrimitivePreference(prefs, PATIENT_ID_KEY, PATIENT_ID_DEFAULT)

    // Calling package of the current session
    override var callingPackage: String by PrimitivePreference(prefs, CALLING_PACKAGE_KEY, CALLING_PACKAGE_DEFAULT)

    // Metadata of the current session
    override var metadata: String by PrimitivePreference(prefs, METADATA_KEY, METADATA_DEFAULT)

    // Result format of the current session
    override var resultFormat: String by PrimitivePreference(prefs, RESULT_FORMAT_KEY, RESULT_FORMAT_DEFAULT)

    override var mainCalloutParameters: MainCalloutParameters
        get() = throw IllegalAccessException("mainCalloutParameters is write only.")
        set(calloutParameters) {
            with(calloutParameters) {
                calloutType = typeParameter.value
                moduleId = moduleIdParameter.value
                userId = userIdParameter.value
                patientId = patientIdParameter.value
                callingPackage = callingPackageParameter.value
                metadata = metadataParameter.value
                resultFormat = resultFormatParameter.value
            }
        }

    override var sessionId: String by PrimitivePreference(prefs, SESSION_ID_KEY, SESSION_ID_DEFAULT)

    // Should the UI automatically slide forward?
    override var nudgeMode: Boolean by PrimitivePreference(prefs, NUDGE_MODE_KEY, NUDGE_MODE_DEFAULT)

    // Has the CHW given consent to use Simprints ID?
    override var consent: Boolean by PrimitivePreference(prefs, CONSENT_KEY, CONSENT_DEFAULT)

    // Threshold that determines the UI feedback for a given fingerprint quality
    override var qualityThreshold: Int by PrimitivePreference(prefs, QUALITY_THRESHOLD_KEY, QUALITY_THRESHOLD_DEFAULT)

    // Number of GUIDs to be returned to the calling app as the result of an identification
    override var returnIdCount: Int by PrimitivePreference(prefs, NB_IDS_KEY, NB_IDS_DEFAULT)

    // Selected language
    override var language: String by PrimitivePreference(prefs, LANGUAGE_KEY, LANGUAGE_DEFAULT)

    // Active language position to be displayed in the list
    override var languagePosition: Int by PrimitivePreference(prefs, LANGUAGE_POSITION_KEY, LANGUAGE_POSITION_DEFAULT)

    // Matcher type
    override var matcherType: Int by PrimitivePreference(prefs, MATCHER_TYPE_KEY, MATCHER_TYPE_DEFAULT)

    // Timeout seconds
    override var timeoutS: Int by PrimitivePreference(prefs, TIMEOUT_KEY, TIMEOUT_DEFAULT)

    // App Key
    override var appKey: String by PrimitivePreference(prefs, APP_KEY_KEY, APP_KEY_DEFAULT)

    // Sync group. Default is user
    override var syncGroup: Constants.GROUP by ComplexPreference(prefs, SYNC_GROUP_KEY, SYNC_GROUP_DEFAULT, groupSerializer)

    // Match group. Default is user
    override var matchGroup: Constants.GROUP by ComplexPreference(prefs, MATCH_GROUP_KEY, MATCH_GROUP_DEFAULT, groupSerializer)

    // Is the vibrate on
    override var vibrateMode: Boolean by PrimitivePreference(prefs, VIBRATE_KEY, VIBRATE_DEFAULT)

    // TODO: document that
    override var matchingEndWaitTimeSeconds: Int by PrimitivePreference(prefs, MATCHING_END_WAIT_TIME_KEY, MATCHING_END_WAIT_TIME_DEFAULT)

    // True if the fingers status should be persisted, false else
    override var fingerStatusPersist: Boolean by PrimitivePreference(prefs, PERSIST_FINGER_KEY, PERSIST_FINGER_DEFAULT)

    override var fingerStatus: Map<FingerIdentifier, Boolean>
            by ComplexPreference(prefs, FINGER_STATUS_KEY, FINGER_STATUS_DEFAULT, fingerIdentifierToBooleanSerializer)

}
