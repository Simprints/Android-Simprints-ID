package com.simprints.id.data.prefs

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.simprints.id.model.Callout
import com.simprints.id.tools.delegates.Preference
import com.simprints.id.tools.delegations.sharedPreferences.ExtSharedPreferences
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier
import timber.log.Timber
import kotlin.reflect.KProperty

class PreferencesManagerImpl(private val prefs: ExtSharedPreferences,
                             private val gson: Gson): PreferencesManager {

    companion object {

        val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        val PREF_MODE = Context.MODE_PRIVATE

        // Session state
        private val API_KEY_KEY = "ApiKey"
        private val API_KEY_DEFAULT = ""

        private val CALLOUT_KEY = "Callout"
        private val CALLOUT_DEFAULT = Callout.NULL

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
        private val FINGER_STATUS_DEFAULT = FingerIdentifier.values().map({ Pair(it, false)}).toMap()
    }


    inner class MapPreference<K: Any, V: Any>(private val name: String, private val default: Map<K, V>) {

        private var initialized: Boolean = false
        private lateinit var field: Map<K, V>

        @Suppress("UNCHECKED_CAST")
        @Synchronized
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Map<K, V> {
            Timber.d("get map ${property.name}")
            if (!initialized) {
                field = if (prefs.contains(name)) {
                    val string = prefs.getString(name, "")
                    Timber.d("Read $string")
                    gson.fromJson(string, Map::class.java) as Map<K, V>
                } else {
                    Timber.d("Pref did not contain, returning default")
                    default
                }
                initialized = true
            }
            return field
        }

        @SuppressLint("CommitPrefEdits")
        @Synchronized
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Map<K, V>) {
            Timber.d("set map ${property.name}")
            field = value
            val string = gson.toJson(value)
            prefs.edit().putString(name, gson.toJson(value)).apply()
            Timber.d("Wrote $string")
        }
    }


    // API key of the current sessions
    override var apiKey: String by Preference(prefs, API_KEY_KEY, API_KEY_DEFAULT)

    // Callout of the current session
    override var callout: Callout by Preference(prefs, CALLOUT_KEY, CALLOUT_DEFAULT)

    // Module ID of the current session
    override var moduleId: String by Preference(prefs, MODULE_ID_KEY, MODULE_ID_DEFAULT)

    // User ID of the current session
    override var userId: String by Preference(prefs, USER_ID_KEY, USER_ID_DEFAULT)

    // Patient ID of the current session
    override var patientId: String by Preference(prefs, PATIENT_ID_KEY, PATIENT_ID_DEFAULT)

    // Calling package of the current session
    override var callingPackage: String by Preference(prefs, CALLING_PACKAGE_KEY, CALLING_PACKAGE_DEFAULT)

    // Metadata of the current session
    override var metadata: String by Preference(prefs, METADATA_KEY, METADATA_DEFAULT)

    // Result format of the current session
    override var resultFormat: String by Preference(prefs, RESULT_FORMAT_KEY, RESULT_FORMAT_DEFAULT)

    // Should the UI automatically slide forward?
    override var nudgeMode: Boolean by Preference(prefs, NUDGE_MODE_KEY, NUDGE_MODE_DEFAULT)

    // Has the CHW given consent to use Simprints ID?
    override var consent: Boolean by Preference(prefs, CONSENT_KEY, CONSENT_DEFAULT)

    // Threshold that determines the UI feedback for a given fingerprint quality
    override var qualityThreshold: Int by Preference(prefs, QUALITY_THRESHOLD_KEY, QUALITY_THRESHOLD_DEFAULT)

    // Number of GUIDs to be returned to the calling app as the result of an identification
    override var returnIdCount: Int by Preference(prefs, NB_IDS_KEY, NB_IDS_DEFAULT)

    // Selected language
    override var language: String by Preference(prefs, LANGUAGE_KEY, LANGUAGE_DEFAULT)

    // Active language position to be displayed in the list
    override var languagePosition: Int by Preference(prefs, LANGUAGE_POSITION_KEY, LANGUAGE_POSITION_DEFAULT)

    // Matcher type
    override var matcherType: Int by Preference(prefs, MATCHER_TYPE_KEY, MATCHER_TYPE_DEFAULT)

    // Timeout seconds
    override var timeoutS: Int by Preference(prefs, TIMEOUT_KEY, TIMEOUT_DEFAULT)

    // App Key
    override var appKey: String by Preference(prefs, APP_KEY_KEY, APP_KEY_DEFAULT)

    // Sync group. Default is user
    override var syncGroup: Constants.GROUP by Preference(prefs, SYNC_GROUP_KEY, SYNC_GROUP_DEFAULT)

    // Match group. Default is user
    override var matchGroup: Constants.GROUP by Preference(prefs, MATCH_GROUP_KEY, MATCH_GROUP_DEFAULT)

    // Is the vibrate on
    override var vibrateMode: Boolean by Preference(prefs, VIBRATE_KEY, VIBRATE_DEFAULT)

    // TODO: document that
    override var matchingEndWaitTimeS: Int by Preference(prefs, MATCHING_END_WAIT_TIME_KEY, MATCHING_END_WAIT_TIME_DEFAULT)

    // True if the fingers status should be persisted, false else
    override var fingerStatusPersist: Boolean by Preference(prefs, PERSIST_FINGER_KEY, PERSIST_FINGER_DEFAULT)

    private var fingerStatus: Map<FingerIdentifier, Boolean> by MapPreference(FINGER_STATUS_KEY, FINGER_STATUS_DEFAULT)

    /**
     * Get the status of a specific finger.
     *
     * @param fingerIdentifier The finger status to retrieve
     * @return FingerConfig
     */
    override fun getFingerStatus(fingerIdentifier: FingerIdentifier): Boolean {
        return fingerStatus[fingerIdentifier] ?: throw Error("Missing finger status")
    }


    /**
     * Set the status of a specific finger
     *
     * @param fingerIdentifier selected finger
     * @param show             True = show, False = don't show
     */
    override fun setFingerStatus(fingerIdentifier: FingerIdentifier, show: Boolean) {
        fingerStatus = fingerStatus.toMutableMap().apply { put(fingerIdentifier, show) }
    }
}
