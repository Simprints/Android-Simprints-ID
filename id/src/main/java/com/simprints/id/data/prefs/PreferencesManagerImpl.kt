package com.simprints.id.data.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.tools.extensions.getEnum
import com.simprints.id.tools.extensions.putEnum
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier
import kotlin.reflect.KProperty

class PreferencesManagerImpl(context: Context, preferenceFileName: String = PREF_FILE_NAME): PreferencesManager {

    companion object {

        private val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"

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

        private val LAST_USER_ID_KEY = "LastUserId"
        private val LAST_USER_ID_DEFAULT = ""

        private val PERSIST_FINGER_KEY = "PersistFingerStatus"
        private val PERSIST_FINGER_DEFAULT = false
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)

    inner class Preference<T>(private val name: String, private val default: T) {

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
                with(prefs) {
                    when (default) {
                        is Long -> prefs.getLong(name, default)
                        is String -> prefs.getString(name, default)
                        is Int -> prefs.getInt(name, default)
                        is Boolean -> prefs.getBoolean(name, default)
                        is Float -> prefs.getFloat(name, default)
                        is Constants.GROUP -> prefs.getEnum(name, default)
                        else -> throw IllegalArgumentException("This type can be saved into Preferences")
                    } as T
                }

        @SuppressLint("CommitPrefEdits")
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            with(prefs.edit()) {
                when (value) {
                    is Long -> putLong(name, value)
                    is String -> putString(name, value)
                    is Int -> putInt(name, value)
                    is Boolean -> putBoolean(name, value)
                    is Float -> putFloat(name, value)
                    is Constants.GROUP -> putEnum(name, value)
                    else -> throw IllegalArgumentException("This type can't be saved into Preferences")
                }.apply()
            }
        }
    }


    // Should the UI automatically slide forward?
    override var nudgeMode: Boolean by Preference(NUDGE_MODE_KEY, NUDGE_MODE_DEFAULT)

    // Has the CHW given consent to use Simprints ID?
    override var consent: Boolean by Preference(CONSENT_KEY, CONSENT_DEFAULT)

    // Threshold that determines the UI feedback for a given fingerprint quality
    override var qualityThreshold: Int by Preference(QUALITY_THRESHOLD_KEY, QUALITY_THRESHOLD_DEFAULT)

    // Number of GUIDs to be returned to the calling app as the result of an identification
    override var returnIdCount: Int by Preference(NB_IDS_KEY, NB_IDS_DEFAULT)

    // Selected language
    override var language: String by Preference(LANGUAGE_KEY, LANGUAGE_DEFAULT)

    // Active language position to be displayed in the list
    override var languagePosition: Int by Preference(LANGUAGE_POSITION_KEY, LANGUAGE_POSITION_DEFAULT)

    // Matcher type
    override var matcherType: Int by Preference(MATCHER_TYPE_KEY, MATCHER_TYPE_DEFAULT)

    // Timeout seconds
    override var timeoutS: Int by Preference(TIMEOUT_KEY, TIMEOUT_DEFAULT)

    // App Key
    override var appKey: String by Preference(APP_KEY_KEY, APP_KEY_DEFAULT)

    // Sync group. Default is user
    override var syncGroup: Constants.GROUP by Preference(SYNC_GROUP_KEY, SYNC_GROUP_DEFAULT)

    // Match group. Default is user
    override var matchGroup: Constants.GROUP by Preference(MATCH_GROUP_KEY, MATCH_GROUP_DEFAULT)

    // Is the vibrate on
    override var vibrateMode: Boolean by Preference(VIBRATE_KEY, VIBRATE_DEFAULT)

    // TODO: document that
    override var matchingEndWaitTimeS: Int by Preference(MATCHING_END_WAIT_TIME_KEY, MATCHING_END_WAIT_TIME_DEFAULT)

    // ID of the last user
    override var lastUserId: String by Preference(LAST_USER_ID_KEY, LAST_USER_ID_DEFAULT)

    // True if the fingers status should be persisted, false else
    override var fingerStatusPersist: Boolean by Preference(PERSIST_FINGER_KEY, PERSIST_FINGER_DEFAULT)

    /**
     * Get the status of a specific finger.
     *
     * @param fingerIdentifier The finger status to retrieve
     * @return FingerConfig
     */
    override fun getFingerStatus(fingerIdentifier: FingerIdentifier): Boolean =
            prefs.getBoolean(fingerIdentifier.toString(), false)

    /**
     * Set the status of a specific finger
     *
     * @param fingerIdentifier selected finger
     * @param show             True = show, False = don't show
     */
    override fun setFingerStatus(fingerIdentifier: FingerIdentifier, show: Boolean) =
            prefs.edit().putBoolean(fingerIdentifier.toString(), show).apply()
}