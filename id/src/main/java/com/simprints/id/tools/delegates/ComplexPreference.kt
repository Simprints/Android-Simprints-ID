package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
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
class ComplexPreference<T:Any> (prefs: ImprovedSharedPreferences,
                                private val key: String,
                                defValue: T,
                                private val serializer: Serializer<T>) {

    private val serializedDefValue = serializer.serialize(defValue)
    private var serializedValue by PrimitivePreference(prefs, key, serializedDefValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        Timber.d("ComplexPreference.getValue $key")
        return serializer.deserialize(serializedValue)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        Timber.d("ComplexPreference.setValue $key")
        serializedValue = serializer.serialize(value)
    }
}
