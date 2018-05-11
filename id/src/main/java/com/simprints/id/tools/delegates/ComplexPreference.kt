package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unsafe.MismatchedTypeError
import com.simprints.id.tools.serializers.EnumSerializer
import com.simprints.id.tools.serializers.Serializer
import timber.log.Timber
import kotlin.reflect.KProperty

/**
 * Delegate to read/write any type to Shared Preferences.
 *
 * Backed by a PrimitivePreference<String>, after serialization with the specified serializer.
 * Thus, has the same guarantees as PrimitivePreference.
 *
 * @author etienne@simprints.com
 */
class ComplexPreference<T:Any> (val prefs: ImprovedSharedPreferences,
                                private val key: String,
                                defValue: T,
                                private val serializer: Serializer<T>) {

    private val serializedDefValue = serializer.serialize(defValue)
    private var serializedValue by PrimitivePreference(prefs, key, serializedDefValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        Timber.d("ComplexPreference.getValue $key")

        return try {
            serializer.deserialize(serializedValue)
        } catch (e: MismatchedTypeError){
            ifEnumDeserializationThenWeTryReadEnumIndex() ?: throw e
        }
    }

    /**
     * if we deserialize an Enum and we get a MismatchedTypeError
     * really likely that is because in the SharedPreference an integer (enum index) instead of String (enum name) is stored.
     * It can happen when we change the type of a value stored in the SharedPref between versions
     * without a migration process (as we do for Realm).
     * https://fabric.io/simprints/android/apps/com.simprints.id/issues/5af29f5111e9fa0aa5f16cd8/sessions/latest?build=78388335
     * https://github.com/Simprints/Android-Simprints-ID/commit/ae3c73e1d83739f0fc72a477f6f3f4576d223071#diff-b72b83e1b06c25b717b75c6919528a39
     * So tentatively we try to build the enum from the integer saved in the sharedPref.
     */
    private fun ifEnumDeserializationThenWeTryReadEnumIndex(): T? {
        try {
            if(serializer is EnumSerializer) {
                val serializedValue = prefs.getPrimitive(key, -1)
                if (serializedValue > -1) {
                    return serializer.deserialize(serializedValue)
                }
            }
        } catch (t: Throwable){ t.printStackTrace() }

        return null
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        Timber.d("ComplexPreference.setValue $key")
        serializedValue = serializer.serialize(value)
    }
}
