package com.simprints.infra.network.exceptions

class BackendMaintenanceException(
    message: String = "BFSID Maintenance Exception",
    val estimatedOutage: Long?,
) : RuntimeException(message)
