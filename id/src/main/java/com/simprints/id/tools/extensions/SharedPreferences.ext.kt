package com.simprints.id.tools.extensions

import android.content.SharedPreferences

fun SharedPreferences.save(transaction: (SharedPreferences.Editor) -> Unit) {
    with(this.edit()) {
        transaction(this)
        this.commit()
    }
}
