package com.simprints.id

import com.simprints.id.tools.logging.LoggingConfigHelper
import com.simprints.id.tools.logging.TimberDebugLoggingConfigHelper

@Suppress("unused")
class Logging : Application() {

    override var loggingConfigHelper: LoggingConfigHelper = TimberDebugLoggingConfigHelper()

}
