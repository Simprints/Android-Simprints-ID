package com.simprints.moduleapi.common

import android.os.Parcelable

interface IPath : Parcelable {
    val dirs: Array<String>

    fun compose(): String
    fun remove(dir: String): IPath
    fun remove(subPath: IPath): IPath
    fun remove(subset: Array<String>): IPath
}
