package com.simprints.infra.security.keyprovider

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

/**
 * Creates a Flow that emits the value of the given key in the SharedPreferences whenever it changes.
 * Optionally, it can emit the current value immediately upon collection.
 *
 * @param key The key to observe in the SharedPreferences.
 * @param default The default value to emit if the key is not present.
 * @param emitInitial If true, the current value will be emitted immediately upon collection.
 * @return A Flow emitting the value associated with the key, or the default value if not present.
 */
inline fun <reified T> SharedPreferences.keyFlow(
    key: String,
    default: T,
    emitInitial: Boolean = true,
): Flow<T> = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
        if (changedKey == key) {
            val newValue = prefs.all[key] as? T ?: default
            trySend(newValue)
        }
    }
    registerOnSharedPreferenceChangeListener(listener)
    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}.let { base ->
    val withInitial = if (emitInitial) {
        base.onStart { emit(this@keyFlow.all[key] as? T ?: default) }
    } else {
        base
    }
    withInitial.distinctUntilChanged()
}
