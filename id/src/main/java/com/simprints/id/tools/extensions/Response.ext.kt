package com.simprints.id.tools.extensions

import com.simprints.core.network.NetworkConstants
import retrofit2.Response

fun <T> Response<T>.isBackendMaitenanceException(): Boolean {
    if (this.code() == 503) {
        val jsonReseponse = this.errorBody()?.string()?.filterNot { it.isWhitespace() }
        return jsonReseponse != null && jsonReseponse.contains(NetworkConstants.BACKEND_MAINTENANCE_ERROR_STRING)
    }
    return false
}
