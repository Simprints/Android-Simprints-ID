package com.simprints.fingerprint.activities.connect.result

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class FetchOtaResult(val isMaintenanceMode: Boolean = false, val estimatedOutage:Long? = null) : Parcelable
