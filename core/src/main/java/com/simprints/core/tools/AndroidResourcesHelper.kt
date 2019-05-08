package com.simprints.core.tools

import android.graphics.drawable.Drawable

interface AndroidResourcesHelper {

    fun getString(res: Int): String

    fun getDrawable(res: Int): Drawable?
}
