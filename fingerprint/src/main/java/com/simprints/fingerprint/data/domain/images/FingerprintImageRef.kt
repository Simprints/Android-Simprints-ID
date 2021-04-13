package com.simprints.fingerprint.data.domain.images

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class FingerprintImageRef(val path: Path) : Parcelable
