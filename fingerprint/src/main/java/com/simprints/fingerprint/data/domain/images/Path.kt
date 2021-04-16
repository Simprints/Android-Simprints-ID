package com.simprints.fingerprint.data.domain.images

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Path(val parts: Array<String>) : Parcelable
