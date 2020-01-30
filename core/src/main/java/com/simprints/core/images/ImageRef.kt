package com.simprints.core.images

import android.os.Parcelable
import com.simprints.moduleapi.common.IPath

abstract class ImageRef(open val path: IPath) : Parcelable
