package com.simprints.id.data.prefs.improvedSharedPreferences

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.simprints.id.exceptions.unsafe.MismatchedTypeError
import com.simprints.id.exceptions.unsafe.NonPrimitiveTypeError


class ImprovedSharedPreferencesImpl(private val prefs: SharedPreferences)
    : ImprovedSharedPreferences {

    @SuppressLint("CommitPrefEdits")
    override fun edit(): ImprovedSharedPreferences.Editor =
            ImprovedSharedPreferencesEditorImpl(prefs.edit())

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue)

    override fun <T: Any> getPrimitive(key: String, defaultValue: T): T =
            try {
                tryGetPrimitive(key, defaultValue)
            } catch (nonPrimitiveTypeError: NonPrimitiveTypeError) {
                throw nonPrimitiveTypeError
            } catch (anyOtherException: Throwable) {
                val msg = "Value stored for key $key is not a ${defaultValue.javaClass.simpleName}."
                throw MismatchedTypeError(msg, anyOtherException)
            }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> tryGetPrimitive(key: String, defaultValue: T): T =
            when (defaultValue) {
                is Byte ->  getByte(key, defaultValue) as T
                is Short -> getShort(key, defaultValue) as T
                is Int -> prefs.getInt(key, defaultValue) as T
                is Long -> prefs.getLong(key, defaultValue) as T
                is Float -> prefs.getFloat(key, defaultValue) as T
                is Double -> getDouble(key, defaultValue) as T
                is String -> prefs.getString(key, defaultValue) as T
                is Boolean -> prefs.getBoolean(key, defaultValue) as T
                else -> throw NonPrimitiveTypeError.forTypeOf(defaultValue)
            }

    private fun getByte(key: String, defaultValue: Byte): Byte =
        prefs.getInt(key, defaultValue.toInt()).toByte()

    private fun getShort(key: String, defaultValue: Short): Short =
        prefs.getInt(key, defaultValue.toInt()).toShort()

    private fun getDouble(key: String, defaultValue: Double): Double =
            Double.fromBits(prefs.getLong(key, defaultValue.toRawBits()))

}

