package com.simprints.id.tools.googleapis

import android.app.Activity
import com.simprints.id.domain.alert.AlertType

interface GooglePlayServicesAvailabilityChecker {
    fun check(activity: Activity, launchAlert: (AlertType) -> Unit)
}
