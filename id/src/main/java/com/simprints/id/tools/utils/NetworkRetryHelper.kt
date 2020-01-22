package com.simprints.id.tools.utils

import com.simprints.core.network.NetworkConstants
import com.simprints.core.network.SimRemoteInterface
import com.simprints.core.tools.coroutines.retryIO
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.tools.extensions.isClientAndCloudIntegrationIssue
import com.simprints.id.tools.extensions.trace

suspend fun <T, V : SimRemoteInterface> retrySimNetworkCalls(client: V,
                                                             networkBlock: suspend (V) -> T,
                                                             traceName: String? = null): T {
    val trace = if (traceName != null) {
        trace(traceName)
    } else null

    return retryIO(
        times = NetworkConstants.RETRY_ATTEMPTS_FOR_NETWORK_CALLS,
        runBlock = {
            return@retryIO try {
                networkBlock(client).also {
                    trace?.stop()
                }
            } catch (throwable: Throwable) {
                throw if (throwable.isClientAndCloudIntegrationIssue()) {
                    SyncCloudIntegrationException("Http status code not worth to retry", throwable)
                } else {
                    throwable
                }
            }
        },
        retryIf = { it !is SyncCloudIntegrationException })
}
