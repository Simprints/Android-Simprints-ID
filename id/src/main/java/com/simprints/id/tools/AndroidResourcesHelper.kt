package com.simprints.id.tools

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.simprints.id.Application

interface AndroidResourcesHelper {

    fun getString(res: Int): String
    fun getStringArray(res: Int): Array<String>
    fun getString(resId: Int, params: Array<Any>): String

    fun getDrawable(res: Int): Drawable?
    fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String
    fun getColor(color: Int): ColorStateList?

    companion object {
        fun build(app: Application): AndroidResourcesHelper =
            app.component.getAndroidResourcesHelper()
    }
}
