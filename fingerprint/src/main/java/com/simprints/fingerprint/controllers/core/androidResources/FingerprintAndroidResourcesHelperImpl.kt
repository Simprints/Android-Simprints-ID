package com.simprints.fingerprint.controllers.core.androidResources

import android.graphics.drawable.Drawable
import com.simprints.id.tools.AndroidResourcesHelper

class FingerprintAndroidResourcesHelperImpl(private val coreAndroidResourcesHelper: AndroidResourcesHelper) : FingerprintAndroidResourcesHelper {

    override fun getString(res: Int): String = coreAndroidResourcesHelper.getString(res)
    override fun getStringArray(res: Int): Array<String> = coreAndroidResourcesHelper.getStringArray(res)
    override fun getString(resId: Int, vararg formatArgs: Any): String = coreAndroidResourcesHelper.getString(resId, formatArgs)
    override fun getDrawable(res: Int): Drawable? = coreAndroidResourcesHelper.getDrawable(res)
}
