package com.simprints.fingerprint.controllers.core.simnetworkutils

import com.simprints.id.tools.utils.SimNetworkUtils.Connection

interface FingerprintSimNetworkUtils {

    val mobileNetworkType: String?
    val connectionsStates: List<Connection>
    fun isConnected():Boolean
}
