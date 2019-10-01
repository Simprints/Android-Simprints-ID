package com.simprints.core.images

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SecuredImageRef(override val uri: String) : ImageRef(uri), Parcelable
