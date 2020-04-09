package com.simprints.face.controllers.core.androidResources

import android.graphics.drawable.Drawable

interface FaceAndroidResourcesHelper {

    fun getString(res: Int): String
    fun getStringArray(res: Int): Array<String>
    fun getString(resId: Int, params: Array<Any>): String
    fun getDrawable(res: Int): Drawable?
    fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String
}
