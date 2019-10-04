package com.simprints.id.tools.extensions

import android.content.SharedPreferences

fun SharedPreferences.Editor.putMap(key: String, map: Map<String, String> = emptyMap()) {
    putStringSet("${key}_keys", map.keys)
    putStringSet("${key}_values", map.values.toSet())
}


fun SharedPreferences.getMap(key: String, default: Map<String, String> = emptyMap()): Map<String, String>  {
    val keys = getStringSet("${key}_keys", emptySet())
    val values = getStringSet("${key}_values", emptySet())
    return keys?.zip(values)?.toMap() ?: default
}


fun SharedPreferences.save(transaction: (SharedPreferences.Editor) -> Unit) {
    with(this.edit()) {
        transaction(this)
        this.apply()
    }
}
