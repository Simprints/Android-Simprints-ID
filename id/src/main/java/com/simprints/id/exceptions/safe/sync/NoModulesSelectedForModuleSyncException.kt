package com.simprints.id.exceptions.safe.sync

import com.simprints.id.exceptions.safe.SafeException

class NoModulesSelectedForModuleSyncException(message: String = "No modules selected for module sync"):
    SafeException(message)
