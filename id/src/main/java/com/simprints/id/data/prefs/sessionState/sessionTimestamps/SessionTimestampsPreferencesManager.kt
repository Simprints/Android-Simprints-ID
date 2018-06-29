package com.simprints.id.data.prefs.sessionState.sessionTimestamps


interface SessionTimestampsPreferencesManager {

    var msSinceBootOnSessionStart: Long
    var msSinceBootOnLoadEnd: Long
    var msSinceBootOnMainStart: Long
    var msSinceBootOnMatchStart: Long
    var msSinceBootOnSessionEnd: Long

    fun initializeSessionTimestamps(msSinceBootOnSessionStart: Long)

}
