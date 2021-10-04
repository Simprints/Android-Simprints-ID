package com.simprints.id.tools.utils

import android.content.Context
import android.net.ConnectivityManager
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.Connection

open class SimNetworkUtilsImpl(val ctx: Context) : SimNetworkUtils {

    private val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override var connectionsStates: List<Connection> =
        arrayListOf<Connection>().apply {
            try {
                connectivityManager.allNetworkInfo.map {
                    if (it.typeName != null && it.detailedState != null) {
                        add(Connection(it.typeName, it.detailedState))
                    }
                }

            } catch (e: Exception) {
            }
        }

}
