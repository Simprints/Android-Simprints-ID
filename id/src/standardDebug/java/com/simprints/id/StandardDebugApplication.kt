package com.simprints.id

import com.simprints.id.tools.logging.LoggingConfigHelper
import com.simprints.id.tools.logging.TimberLineNumberLoggingConfigHelper

@Suppress("unused")
class StandardDebugApplication : Application() {

    override var loggingConfigHelper: LoggingConfigHelper = TimberLineNumberLoggingConfigHelper()

}
