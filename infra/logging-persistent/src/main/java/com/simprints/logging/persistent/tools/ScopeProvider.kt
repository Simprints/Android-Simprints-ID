package com.simprints.logging.persistent.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Since the module cannot rely on infra:core it must provide its own dispatcher and scope definitions.
 */
@Singleton
internal class ScopeProvider @Inject constructor() {
    val dispatcherIO = Dispatchers.IO

    val externalScope = CoroutineScope(SupervisorJob() + dispatcherIO)
}
