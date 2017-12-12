package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import com.simprints.id.exceptions.safe.NonPrimitiveTypeException


class ImprovedSharedPreferencesEditorImpl(private val editor: SharedPreferences.Editor)
    : ImprovedSharedPreferences.Editor,
        SharedPreferences.Editor by editor {

    override fun <T: Any> putPrimitive(key: String, value: T): ImprovedSharedPreferences.Editor =
            this.apply {
                when (value) {
                    is Byte -> putByte(key, value)
                    is Short -> putShort(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> throw NonPrimitiveTypeException.forTypeOf(value)
                }
            }

    private fun putByte(key: String, value: Byte): ImprovedSharedPreferences.Editor =
            this.apply { putInt(key, value.toInt()) }

    private fun putShort(key: String, value: Short): ImprovedSharedPreferences.Editor =
            this.apply { putInt(key, value.toInt()) }

    private fun putDouble(key: String, value: Double): ImprovedSharedPreferences.Editor =
            this.apply { putLong(key, value.toRawBits()) }


}