package com.simprints.id.tools.logging

import timber.log.Timber

class TimberLineNumberLoggingConfigHelper : LoggingConfigHelper() {

    override fun setUpLogging() {
        Timber.plant(LineNumberLoggingTree())
        Timber.d("Line number logging set.")
    }

}
