package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences
import com.simprints.id.exceptions.unsafe.NonPrimitiveTypeError


class ImprovedSharedPreferencesEditorImpl(private val editor: SharedPreferences.Editor)
    : ImprovedSharedPreferences.Editor {

    override fun <T: Any> putPrimitive(key: String, value: T): ImprovedSharedPreferences.Editor =
            this.apply {
                when (value) {
                    is Byte -> putByte(key, value)
                    is Short -> putShort(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    else -> throw NonPrimitiveTypeError.forTypeOf(value)
                }
            }

    override fun commit() {
        editor.commit()
    }

    override fun apply() {
        editor.apply()
    }

    private fun putByte(key: String, value: Byte): ImprovedSharedPreferences.Editor =
            this.apply { editor.putInt(key, value.toInt()) }

    private fun putShort(key: String, value: Short): ImprovedSharedPreferences.Editor =
            this.apply { editor.putInt(key, value.toInt()) }

    private fun putDouble(key: String, value: Double): ImprovedSharedPreferences.Editor =
            this.apply { editor.putLong(key, value.toRawBits()) }


}
