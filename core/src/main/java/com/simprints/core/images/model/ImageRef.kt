package com.simprints.core.images.model

import android.os.Parcelable
import com.simprints.moduleapi.common.IPath

abstract class ImageRef(open val path: IPath) : Parcelable
