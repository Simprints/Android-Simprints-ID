package com.simprints.id.tools.extensions

import android.content.SharedPreferences
import java.util.concurrent.locks.ReentrantLock

private val sharedCounterLock = ReentrantLock()

fun SharedPreferences.Editor.putMap(key: String, map: Map<String, String> = emptyMap()) {
    sharedCounterLock.lock()
    putStringSet("${key}_keys", map.keys)
    putStringSet("${key}_values", map.values.toSet())
    sharedCounterLock.unlock()
}


fun SharedPreferences.getMap(key: String, default: Map<String, String> = emptyMap()): Map<String, String> {
    sharedCounterLock.lock()
    val keys = getStringSet("${key}_keys", emptySet())
    val values = getStringSet("${key}_values", emptySet())
    sharedCounterLock.unlock()

    return keys?.zip(values)?.toMap() ?: default
}


fun SharedPreferences.save(transaction: (SharedPreferences.Editor) -> Unit) {
    with(this.edit()) {
        transaction(this)
        this.apply()
    }
}
