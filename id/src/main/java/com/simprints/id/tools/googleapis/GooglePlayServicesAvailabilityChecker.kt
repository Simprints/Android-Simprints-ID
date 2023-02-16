package com.simprints.id.tools.googleapis

import android.app.Activity

interface GooglePlayServicesAvailabilityChecker {
    fun check(activity: Activity)
}
