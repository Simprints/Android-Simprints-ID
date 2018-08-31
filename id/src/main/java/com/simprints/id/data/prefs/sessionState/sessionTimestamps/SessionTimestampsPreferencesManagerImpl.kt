package com.simprints.id.data.prefs.sessionState.sessionTimestamps

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference

class SessionTimestampsPreferencesManagerImpl(prefs: ImprovedSharedPreferences)
    : SessionTimestampsPreferencesManager {

    companion object {

        private val MS_SINCE_BOOT_ON_SESSION_START_KEY = "MsSinceBootOnSessionStart"
        private val MS_SINCE_BOOT_ON_SESSION_START_DEFAULT = 0L

        private val MS_SINCE_BOOT_ON_LOAD_END_KEY = "MsSinceBootOnLoadEnd"
        private val MS_SINCE_BOOT_ON_LOAD_END_DEFAULT = -1L

        private val MS_SINCE_BOOT_ON_MAIN_START_KEY = "MsSinceBootOnMainStart"
        private val MS_SINCE_BOOT_ON_MAIN_START_DEFAULT = -1L

        private val MS_SINCE_BOOT_ON_MATCH_START_KEY = "MsSinceBootOnMatchStart"
        private val MS_SINCE_BOOT_ON_MATCH_START_DEFAULT = -1L

        private val MS_SINCE_BOOT_ON_SESSION_END_KEY = "MsSinceBootOnSessionEnd"
        private val MS_SINCE_BOOT_ON_SESSION_END_DEFAULT = -1L
    }

    // Milliseconds since boot, on current session start
    override var msSinceBootOnSessionStart: Long
        by PrimitivePreference(prefs, MS_SINCE_BOOT_ON_SESSION_START_KEY, MS_SINCE_BOOT_ON_SESSION_START_DEFAULT)

    // Milliseconds elapsed between current activeSession started, and current activeSession loading ended.
    override var msSinceBootOnLoadEnd: Long
        by PrimitivePreference(prefs, MS_SINCE_BOOT_ON_LOAD_END_KEY, MS_SINCE_BOOT_ON_LOAD_END_DEFAULT)

    // Milliseconds elapsed between current session started and collectFingerprintsActivity started.
    override var msSinceBootOnMainStart: Long
        by PrimitivePreference(prefs, MS_SINCE_BOOT_ON_MAIN_START_KEY, MS_SINCE_BOOT_ON_MAIN_START_DEFAULT)

    // Milliseconds elapsed between current session started and matching started.
    override var msSinceBootOnMatchStart: Long
        by PrimitivePreference(prefs, MS_SINCE_BOOT_ON_MATCH_START_KEY, MS_SINCE_BOOT_ON_MATCH_START_DEFAULT)

    // Milliseconds elapsed between current session started and it ended.
    override var msSinceBootOnSessionEnd: Long
        by PrimitivePreference(prefs, MS_SINCE_BOOT_ON_SESSION_END_KEY, MS_SINCE_BOOT_ON_SESSION_END_DEFAULT)

    override fun initializeSessionTimestamps(msSinceBootOnSessionStart: Long) {
        this.msSinceBootOnSessionStart = msSinceBootOnSessionStart
        msSinceBootOnLoadEnd = msSinceBootOnSessionStart - 1
        msSinceBootOnMainStart = msSinceBootOnSessionStart - 1
        msSinceBootOnMatchStart = msSinceBootOnSessionStart - 1
        msSinceBootOnSessionEnd = msSinceBootOnSessionStart - 1
    }
}
