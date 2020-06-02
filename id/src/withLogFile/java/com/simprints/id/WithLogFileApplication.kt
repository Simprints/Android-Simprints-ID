package com.simprints.id

import com.simprints.id.tools.logging.LoggingConfigHelper
import com.simprints.id.tools.logging.TimberFileLoggingConfigHelper

@Suppress("unused")
class WithLogFileApplication : Application() {

    override var loggingConfigHelper: LoggingConfigHelper = TimberFileLoggingConfigHelper()

}
