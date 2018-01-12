package com.simprints.id.data.prefs

import android.content.Context
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.domain.Location
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.tools.delegates.ComplexPreference
import com.simprints.id.tools.delegates.PrimitivePreference
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier


class PreferencesManagerImpl(prefs: ImprovedSharedPreferences,
                             fingerIdentifierToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                             calloutActionSerializer: Serializer<CalloutAction>,
                             groupSerializer: Serializer<Constants.GROUP>,
                             locationSerializer: Serializer<Location>): PreferencesManager {

    companion object {

        val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        val PREF_MODE = Context.MODE_PRIVATE

        // Main Callout Parameters

        private val CALLOUT_ACTION_KEY = "CalloutAction"
        private val CALLOUT_ACTION_DEFAULT = CalloutAction.MISSING

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

        private val APP_KEY_KEY = "AppKey"
        private val APP_KEY_DEFAULT = ""

        // Other session state

        private val SESSION_ID_KEY = "SessionId"
        private val SESSION_ID_DEFAULT = ""

        private val MAC_ADDRESS_KEY = "MacAddress"
        private val MAC_ADDRESS_DEFAULT = ""

        private val HARDWARE_VERSION_KEY = "HardwareVersion"
        private val HARDWARE_VERSION_DEFAULT: Short = -1

        private val SCANNER_ID_KEY = "ScannerId"
        private val SCANNER_ID_DEFAULT = ""

        private val LOCATION_KEY = "Location"
        private val LOCATION_DEFAULT = Location("", "")

        private val ELAPSED_REALTIME_ON_SESSION_START_KEY = "ElapsedRealtimeOnSessionStart"
        private val ELAPSED_REALTIME_ON_SESSION_START_DEFAULT = 0L

        private val ELAPSED_REALTIME_ON_LOAD_END_KEY = "ElapsedRealtimeOnLoadEnd"
        private val ELAPSED_REALTIME_ON_LOAD_END_DEFAULT = -1L

        private val ELAPSED_REALTIME_ON_MAIN_START_KEY = "ElapsedRealtimeOnMainStart"
        private val ELAPSED_REALTIME_ON_MAIN_START_DEFAULT = -1L

        private val ELAPSED_REALTIME_ON_MATCH_START_KEY = "ElapsedRealtimeOnMatchStart"
        private val ELAPSED_REALTIME_ON_MATCH_START_DEFAULT = -1L

        private val ELAPSED_REALTIME_ON_SESSION_END_KEY = "ElapsedRealtimeOnSessionEnd"
        private val ELAPSED_REALTIME_ON_SESSION_END_DEFAULT = -1L

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

    // CalloutAction of the current session
    override var calloutAction: CalloutAction by ComplexPreference(prefs, CALLOUT_ACTION_KEY, CALLOUT_ACTION_DEFAULT, calloutActionSerializer)

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

    // App Key
    override var appKey: String by PrimitivePreference(prefs, APP_KEY_KEY, APP_KEY_DEFAULT)


    // Unique identifier of the current session
    override var sessionId: String by PrimitivePreference(prefs, SESSION_ID_KEY, SESSION_ID_DEFAULT)

    // Mac address of the scanner used for the current session
    override var macAddress: String by PrimitivePreference(prefs, MAC_ADDRESS_KEY, MAC_ADDRESS_DEFAULT)

    // Firmware version of the scanner used for the current session
    override var hardwareVersion: Short by PrimitivePreference(prefs, HARDWARE_VERSION_KEY, HARDWARE_VERSION_DEFAULT)

    // Unique identifier of the scanner used for the current session
    override var scannerId: String by PrimitivePreference(prefs, SCANNER_ID_KEY, SCANNER_ID_DEFAULT)

    // Last known location
    override var location: Location by ComplexPreference(prefs, LOCATION_KEY, LOCATION_DEFAULT, locationSerializer)

    // Milliseconds since boot, on current session start, as returned by SystemClock.elapsedRealtime()
    override var elapsedRealtimeOnSessionStart: Long by PrimitivePreference(prefs,
        ELAPSED_REALTIME_ON_SESSION_START_KEY, ELAPSED_REALTIME_ON_SESSION_START_DEFAULT)

    // Milliseconds elapsed between current session started, and current session loading ended.
    override var elapsedRealtimeOnLoadEnd: Long by PrimitivePreference(prefs,
        ELAPSED_REALTIME_ON_LOAD_END_KEY, ELAPSED_REALTIME_ON_LOAD_END_DEFAULT)

    // Milliseconds elapsed between current session started and main activity started.
    override var elapsedRealtimeOnMainStart: Long by PrimitivePreference(prefs,
        ELAPSED_REALTIME_ON_MAIN_START_KEY, ELAPSED_REALTIME_ON_MAIN_START_DEFAULT)

    // Milliseconds elapsed between current session started and matching started.
    override var elapsedRealtimeOnMatchStart: Long by PrimitivePreference(prefs,
        ELAPSED_REALTIME_ON_MATCH_START_KEY, ELAPSED_REALTIME_ON_MATCH_START_DEFAULT)

    // Milliseconds elapsed between current session started and it ended.
    override var elapsedRealtimeOnSessionEnd: Long by PrimitivePreference(prefs,
        ELAPSED_REALTIME_ON_SESSION_END_KEY, ELAPSED_REALTIME_ON_SESSION_END_DEFAULT)


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

    override fun initializeSessionState(sessionId: String, elapsedRealtimeOnSessionStart: Long) {
        calloutAction = CALLOUT_ACTION_DEFAULT
        moduleId = MODULE_ID_DEFAULT
        userId = USER_ID_DEFAULT
        patientId = PATIENT_ID_DEFAULT
        callingPackage = CALLING_PACKAGE_DEFAULT
        metadata = METADATA_DEFAULT
        resultFormat = RESULT_FORMAT_DEFAULT
        appKey = APP_KEY_DEFAULT
        this.sessionId = sessionId
        macAddress = MAC_ADDRESS_DEFAULT
        hardwareVersion = HARDWARE_VERSION_DEFAULT
        scannerId = SCANNER_ID_DEFAULT
        location = LOCATION_DEFAULT
        this.elapsedRealtimeOnSessionStart = elapsedRealtimeOnSessionStart
        elapsedRealtimeOnLoadEnd = elapsedRealtimeOnSessionStart - 1
        elapsedRealtimeOnMainStart = elapsedRealtimeOnSessionStart - 1
        elapsedRealtimeOnMatchStart = elapsedRealtimeOnSessionStart - 1
        elapsedRealtimeOnSessionEnd = elapsedRealtimeOnSessionStart - 1
    }
}
