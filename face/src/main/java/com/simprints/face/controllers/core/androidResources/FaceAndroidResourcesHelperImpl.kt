package com.simprints.face.controllers.core.androidResources

import android.graphics.drawable.Drawable
import com.simprints.id.tools.AndroidResourcesHelper

class FaceAndroidResourcesHelperImpl(private val coreAndroidResourcesHelper: AndroidResourcesHelper) : FaceAndroidResourcesHelper {
    override fun getString(res: Int): String = coreAndroidResourcesHelper.getString(res)
    override fun getStringArray(res: Int): Array<String> = coreAndroidResourcesHelper.getStringArray(res)
    override fun getString(resId: Int, params: Array<Any>): String = coreAndroidResourcesHelper.getString(resId, params)
    override fun getDrawable(res: Int): Drawable? = coreAndroidResourcesHelper.getDrawable(res)
    override fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String =
        coreAndroidResourcesHelper.getStringPlural(stringQuantityKey, quantity, params)
}
