package com.simprints.id.tools.utils

import android.graphics.drawable.Drawable

interface AndroidResourcesHelper {

    fun getString(res: Int): String

    fun getDrawable(res: Int): Drawable?
}
