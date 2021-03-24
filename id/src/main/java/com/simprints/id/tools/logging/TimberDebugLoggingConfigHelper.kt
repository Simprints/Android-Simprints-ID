package com.simprints.id.tools.logging

import timber.log.Timber

class TimberDebugLoggingConfigHelper : LoggingConfigHelper() {

    override fun setUpLogging() {
        Timber.plant(Timber.DebugTree())
        Timber.d("DEBUG LOGGING SET.")
    }

}
