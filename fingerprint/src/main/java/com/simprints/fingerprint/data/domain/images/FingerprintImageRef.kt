package com.simprints.fingerprint.data.domain.images

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The class represents the image of the fingerprint that was captured
 *
 * @property path  the file path for the stored image
 */
@Parcelize
class FingerprintImageRef(val path: Path) : Parcelable
