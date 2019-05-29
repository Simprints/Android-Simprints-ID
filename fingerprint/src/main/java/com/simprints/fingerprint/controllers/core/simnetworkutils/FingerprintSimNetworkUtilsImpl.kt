package com.simprints.fingerprint.controllers.core.simnetworkutils

import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.id.tools.utils.SimNetworkUtils.Connection

open class FingerprintSimNetworkUtilsImpl(private val simNetworkUtils: SimNetworkUtils) : FingerprintSimNetworkUtils {

    override fun isConnected(): Boolean =
        simNetworkUtils.isConnected()

    override val mobileNetworkType: String?
        get() = simNetworkUtils.mobileNetworkType

    override val connectionsStates: List<Connection>
        get() = simNetworkUtils.connectionsStates
}
