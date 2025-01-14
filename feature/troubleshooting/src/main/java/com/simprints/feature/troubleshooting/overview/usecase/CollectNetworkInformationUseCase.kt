package com.simprints.feature.troubleshooting.overview.usecase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("Mostly system calls")
internal class CollectNetworkInformationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return "No network capabilities available"

        if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return "No internet connection"
        }

        val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val connectionType = if (isWifi) "Connected to Wi-Fi" else "Connected to Cellular"
        val connectionValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return """ 
            $connectionType
            Strength: ${getSignalStrength(networkCapabilities)}
            Internet access: $connectionValidated
            Bandwidth up (kbps): ${networkCapabilities.linkUpstreamBandwidthKbps}
            Bandwidth down (kbps): ${networkCapabilities.linkDownstreamBandwidthKbps}
            """.trimIndent()
    }

    private fun getSignalStrength(networkCapabilities: NetworkCapabilities): String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        "N/A"
    } else {
        val strength = networkCapabilities.signalStrength
        val description = when {
            strength >= -50 -> "Excellent"
            strength >= -70 -> "Good"
            strength >= -80 -> "Fair"
            else -> "Weak"
        }
        "$strength ($description)"
    }
}
