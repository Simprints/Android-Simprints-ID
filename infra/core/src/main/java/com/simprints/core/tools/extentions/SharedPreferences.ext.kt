package com.simprints.core.tools.extentions

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Creates a Flow that emits the value of the given key in the SharedPreferences whenever it changes.
 * Optionally, it can emit the current value immediately upon collection.
 *
 * @param key The key to observe in the SharedPreferences.
 * @param default The default value to emit if the key is not present.
 * @return A Flow emitting the value associated with the key, or the default value if not present.
 */
inline fun <reified T> SharedPreferences.onUpdate(
    key: String,
    default: T,
): Flow<T> = callbackFlow {
    // emit the current value immediately upon collection
    val currentValue = all[key] as? T ?: default
    send(currentValue)
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
        if (changedKey == key) {
            val newValue = prefs.all[key] as? T ?: default
            trySend(newValue)
        }
    }
    registerOnSharedPreferenceChangeListener(listener)
    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}
