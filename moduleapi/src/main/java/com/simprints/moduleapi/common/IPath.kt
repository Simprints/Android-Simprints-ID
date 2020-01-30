package com.simprints.moduleapi.common

import android.os.Parcelable

interface IPath : Parcelable {
    val parts: Array<String>

    fun compose(): String
    fun remove(subPathString: String): IPath
    fun remove(subPath: IPath): IPath
    fun remove(subset: Array<String>): IPath
}
