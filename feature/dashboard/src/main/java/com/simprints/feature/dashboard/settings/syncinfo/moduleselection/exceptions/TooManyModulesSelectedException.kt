package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions

internal class TooManyModulesSelectedException(
    message: String = "TooManyModulesSelectedException",
    val maxNumberOfModules: Int,
) : RuntimeException(message)
