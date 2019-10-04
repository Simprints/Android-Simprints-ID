package com.simprints.id.tools.extensions

import android.content.SharedPreferences

fun SharedPreferences.save(transaction: (SharedPreferences.Editor) -> SharedPreferences.Editor) {
    with(this.edit()) {
        transaction(this)
        this.apply()
    }
}
