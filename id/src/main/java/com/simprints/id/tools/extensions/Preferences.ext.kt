package com.simprints.id.tools.extensions

import androidx.preference.Preference

inline fun <reified V : Any> Preference.setChangeListener(crossinline listener: (V) -> Unit) {
    onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
        listener(value as V)
        true
    }
}

fun Preference.enablePreference() {
    this.isEnabled = true
}
