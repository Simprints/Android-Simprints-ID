package com.simprints.id.data.prefs.improvedSharedPreferences

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.simprints.id.exceptions.safe.MismatchedTypeException
import com.simprints.id.exceptions.safe.NonPrimitiveTypeException


class ImprovedSharedPreferencesImpl(private val prefs: SharedPreferences)
    : ImprovedSharedPreferences,
        SharedPreferences by prefs {

    @SuppressLint("CommitPrefEdits")
    override fun edit(): ImprovedSharedPreferences.Editor =
            ImprovedSharedPreferencesEditorImpl(prefs.edit())

    override fun <T: Any> getPrimitive(key: String, defaultValue: T): T =
            try {
                tryGetPrimitive(key, defaultValue)
            } catch (nonPrimitiveTypeException: NonPrimitiveTypeException) {
                throw nonPrimitiveTypeException
            } catch (anyOtherException: Throwable) {
                val msg = "Value stored for key $key is not a ${defaultValue.javaClass.simpleName}."
                throw MismatchedTypeException(msg, anyOtherException)
            }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> tryGetPrimitive(key: String, defaultValue: T): T =
            when (defaultValue) {
                is Byte ->  getByte(key, defaultValue) as T
                is Short -> getShort(key, defaultValue) as T
                is Int -> getInt(key, defaultValue) as T
                is Long -> getLong(key, defaultValue) as T
                is Float -> getFloat(key, defaultValue) as T
                is Double -> getDouble(key, defaultValue) as T
                is String -> getString(key, defaultValue) as T
                is Boolean -> getBoolean(key, defaultValue) as T
                else -> throw NonPrimitiveTypeException.forTypeOf(defaultValue)
            }

    private fun getByte(key: String, defaultValue: Byte): Byte =
            getInt(key, defaultValue.toInt()).toByte()

    private fun getShort(key: String, defaultValue: Short): Short =
            getInt(key, defaultValue.toInt()).toShort()

    private fun getDouble(key: String, defaultValue: Double): Double =
            Double.fromBits(getLong(key, defaultValue.toRawBits()))

}

