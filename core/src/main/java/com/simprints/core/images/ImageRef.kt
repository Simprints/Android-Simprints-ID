package com.simprints.core.images

import com.simprints.moduleapi.common.IPath
import java.io.File

abstract class ImageRef(open val relativePath: IPath, open val fullPath: String) {

    fun getFileName(): String {
        return File(fullPath).name
    }

}
