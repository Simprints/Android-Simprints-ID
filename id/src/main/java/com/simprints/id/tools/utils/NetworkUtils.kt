package com.simprints.id.tools.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager

class NetworkUtils(ctx: Context) {

    class Connection(val type: String, val state: NetworkInfo.DetailedState)

    private val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun isConnected(): Boolean {
        return cm.activeNetworkInfo.detailedState == NetworkInfo.DetailedState.CONNECTED
    }

    val connectionsStates: List<Connection> =
        arrayListOf<Connection>().apply {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cm.allNetworks?.map {
                        cm.getNetworkInfo(it).let {
                            add(Connection(it.typeName, it.detailedState))
                        }
                    }
                } else {
                    cm.allNetworkInfo.map {
                        add(Connection(it.typeName, it.detailedState))
                    }
                }
            } catch (e: Exception) {
                //The API
                e.printStackTrace()
            }
        }

    val mobileNetworkType: String? =
        tm.networkType.let {
            return@let when (it) {
                TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
                TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev. 0"
                TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev. A"
                TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev. B"
                TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
                TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
                TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
                TelephonyManager.NETWORK_TYPE_IDEN -> "iDen"
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Unknown"
                else -> {
                    null
                }
            }
        }
}
