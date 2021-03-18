package com.simprints.id.tools.logging

import timber.log.Timber

class TimberFileLoggingConfigHelper: LoggingConfigHelper() {
    override fun setUpLogging() {
        Timber.plant(FileLoggingTree())
        Timber.d("File logging set.")
    }

}
