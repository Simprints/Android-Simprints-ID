package com.simprints.id.tools

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.PluralsRes
import com.simprints.id.Application

interface AndroidResourcesHelper {

    fun getString(res: Int): String
    fun getStringArray(res: Int): Array<String>
    fun getString(resId: Int, params: Array<Any>): String
    fun getColour(@ColorRes colourId: Int): Int
    fun getDrawable(res: Int): Drawable?
    fun getStringPlural(stringQuantityKey: Int, quantity: Int, params: Array<Any>): String
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, params: Array<Any>): String
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String
    fun getColorStateList(color: Int): ColorStateList?

    fun getLocaleConfiguration(): Configuration

    companion object {
        fun build(app: Application): AndroidResourcesHelper =
            app.component.getAndroidResourcesHelper()
    }
}
