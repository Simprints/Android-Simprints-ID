package com.simprints.id.exceptions.safe

import com.simprints.core.exceptions.SafeException

class BackendMaintenanceException(message: String = "BFSID Maintenance Exception", estimatedOutage: Long? = null) : SafeException(message)
