package com.simprints.id.exceptions.safe.sync

import com.simprints.core.exceptions.SafeException

class NoModulesSelectedForModuleSyncException(message: String = "No modules selected for module sync"):
    SafeException(message)
