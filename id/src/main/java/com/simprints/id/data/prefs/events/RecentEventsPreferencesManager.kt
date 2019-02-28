package com.simprints.id.data.prefs.events

import java.util.*

interface RecentEventsPreferencesManager {

    var lastMacAddress: String
    var lastScannerId: String
    var lastScannerUsed: String
    var lastUserUsed: String

    var lastIdentificationDate: Date?
    var lastEnrolDate: Date?
    var lastVerificationDate: Date?

}
