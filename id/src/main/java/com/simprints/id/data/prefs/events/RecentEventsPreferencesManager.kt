package com.simprints.id.data.prefs.events

interface RecentEventsPreferencesManager {

    var lastScannerVersion: String
    var lastScannerUsed: String
    var lastUserUsed: String

    var enrolmentsToday: Int
    var identificationsToday: Int
    var verificationsToday: Int

    var lastActivityTime: Long

}
