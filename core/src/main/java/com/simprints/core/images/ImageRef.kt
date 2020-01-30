package com.simprints.core.images

import android.os.Parcelable
import com.simprints.moduleapi.common.IPath
import java.io.File

abstract class ImageRef(open val path: IPath) : Parcelable {

    fun getFileName(): String {
        return File(path.compose()).name
    }

}
