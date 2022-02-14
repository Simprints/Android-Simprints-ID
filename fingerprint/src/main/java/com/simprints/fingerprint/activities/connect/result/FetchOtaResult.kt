package com.simprints.fingerprint.activities.connect.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FetchOtaResult(val isMaintenanceMode: Boolean = false, val estimatedOutage:Long? = null) : Parcelable
