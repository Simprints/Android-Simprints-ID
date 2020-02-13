package com.simprints.fingerprint.data.domain.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Path(val parts: Array<String>) : Parcelable
