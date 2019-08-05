package com.simprints.clientapi.identity

import android.content.Context

abstract class GuidSelectionNotifier(val context: Context) {
    abstract fun showMessage()
}
