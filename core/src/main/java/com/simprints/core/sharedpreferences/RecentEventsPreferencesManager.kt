package com.simprints.core.sharedpreferences

interface RecentEventsPreferencesManager {

    var lastScannerVersion: String
    var lastScannerUsed: String
    var lastUserUsed: String

    var enrolmentsToday: Int
    var identificationsToday: Int
    var verificationsToday: Int

    var lastActivityTime: Long

}
