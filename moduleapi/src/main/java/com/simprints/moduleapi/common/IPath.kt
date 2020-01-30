package com.simprints.moduleapi.common

import android.os.Parcelable

interface IPath : Parcelable {
    val parts: Array<String>
}
