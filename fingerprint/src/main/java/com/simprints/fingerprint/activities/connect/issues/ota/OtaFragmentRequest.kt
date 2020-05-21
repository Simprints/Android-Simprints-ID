package com.simprints.fingerprint.activities.connect.issues.ota

import android.os.Parcelable
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import kotlinx.android.parcel.Parcelize

@Parcelize
class OtaFragmentRequest(val availableOtas: List<AvailableOta>): Parcelable
