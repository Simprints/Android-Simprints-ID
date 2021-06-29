package com.simprints.id.data.prefs.preferenceType

import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unexpected.MismatchedTypeException
import com.simprints.id.tools.serializers.EnumSerializer
import com.simprints.id.tools.serializers.Serializer
import timber.log.Timber
import kotlin.reflect.KProperty

/**
 * Delegate to read/write any type to Shared Preferences.
 *
 * Backed by a PrimitivePreference<String>, after serialization with the specified serializer.
 */
open class ComplexPreference<T : Any>(val prefs: ImprovedSharedPreferences,
                                      private val key: String,
                                      defValue: T,
                                      private val serializer: Serializer<T>) {

    protected val serializedDefValue = serializer.serialize(defValue)
    protected var serializedValue by PrimitivePreference(prefs, key, serializedDefValue)

    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T = try {
        serializer.deserialize(serializedValue)
    } catch (e: MismatchedTypeException) {
        ifEnumDeserializationFailedThenTryIndex() ?: throw e
    }

    /**
     * when we deserialize an Enum, sometimes we build MismatchedTypeException in Fabric.
     * That is because in the SharedPreference an integer (enum index) instead of a String (enum name) is stored.
     * It can happen when we changed the type stored in the SharedPref between versions
     * without a migration process (as we do for Realm). So tentatively we try to
     * build the enum from the integer saved in the sharedPref.
     * More details: SID-175
     */
    private fun ifEnumDeserializationFailedThenTryIndex(): T? {
        try {
            if (serializer is EnumSerializer) {
                val serializedValue = prefs.getPrimitive(key, -1)
                if (serializedValue > -1) {
                    return serializer.deserialize(serializedValue)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        return null
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        Timber.d("ComplexPreference.setValue key=$key , value=$serializedValue")
        serializedValue = serializer.serialize(value)
    }
}
