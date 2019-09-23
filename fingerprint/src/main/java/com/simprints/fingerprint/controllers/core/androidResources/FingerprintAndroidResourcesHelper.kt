package com.simprints.fingerprint.controllers.core.androidResources

import android.graphics.drawable.Drawable

interface FingerprintAndroidResourcesHelper {

    fun getString(res: Int): String
    fun getStringArray(res: Int): Array<String>
    fun getString(resId: Int, vararg formatArgs: Any): String
    fun getDrawable(res: Int): Drawable?
    fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String
}
