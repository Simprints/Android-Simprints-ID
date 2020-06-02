package com.simprints.id.tools.logging

import timber.log.Timber

abstract class LoggingConfigHelper {

    fun loggingNeedsSetUp(): Boolean = Timber.treeCount() <= 0

    abstract fun setUpLogging()

}
