package com.simprints.core.tools.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class SimNetworkUtilsImplTest {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var networkCapabilities: NetworkCapabilities

    @MockK
    lateinit var connectivityManager: ConnectivityManager

    private lateinit var simNetworkUtils: SimNetworkUtils

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setUpNetworkCapabilities()
    }

    private fun setUpNetworkCapabilities() {
        MockKAnnotations.init(this)

        every { connectivityManager.activeNetwork } returns mockk()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.getNetworkCapabilities(any()) } returns networkCapabilities
    }

    @Test
    fun `getConnectionsStates mobile connected but has no internet and wifi not connected`() {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        buildConnections(mobileState = true, wifiState = false)
        // When
        val connectionStates = simNetworkUtils.connectionsStates
        // Then
        verifyConnectionStates(
            connectionStates,
            mobileState = SimNetworkUtils.ConnectionState.DISCONNECTED,
            wifiState = SimNetworkUtils.ConnectionState.DISCONNECTED,
        )
    }

    @Test
    fun `getConnectionsStates mobile connected but not validated and wifi not connected`() {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false
        buildConnections(mobileState = true, wifiState = false)
        // When
        val connectionStates = simNetworkUtils.connectionsStates
        // Then
        verifyConnectionStates(
            connectionStates,
            mobileState = SimNetworkUtils.ConnectionState.DISCONNECTED,
            wifiState = SimNetworkUtils.ConnectionState.DISCONNECTED,
        )
    }

    @Test
    fun `getConnectionsStates mobile connected and verified and wifi not connected`() {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        buildConnections(mobileState = true, wifiState = false)
        // When
        val connectionStates = simNetworkUtils.connectionsStates
        // Then
        verifyConnectionStates(
            connectionStates,
            mobileState = SimNetworkUtils.ConnectionState.CONNECTED,
            wifiState = SimNetworkUtils.ConnectionState.DISCONNECTED,
        )
    }

    @Test
    fun `getConnectionsStates mobile not connected and wifi connected but has no internet`() {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        buildConnections(mobileState = false, wifiState = true)
        // When
        val connectionStates = simNetworkUtils.connectionsStates
        // Then
        verifyConnectionStates(
            connectionStates,
            mobileState = SimNetworkUtils.ConnectionState.DISCONNECTED,
            wifiState = SimNetworkUtils.ConnectionState.DISCONNECTED,
        )
    }

    @Test
    fun `getConnectionsStates mobile not connected and wifi connected but not validated`() {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false
        buildConnections(mobileState = false, wifiState = true)
        // When
        val connectionStates = simNetworkUtils.connectionsStates
        // Then
        verifyConnectionStates(
            connectionStates,
            mobileState = SimNetworkUtils.ConnectionState.DISCONNECTED,
            wifiState = SimNetworkUtils.ConnectionState.DISCONNECTED,
        )
    }

    @Test
    fun `getConnectionsStates mobile not  connected and wifi  connected and  verified`() {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        buildConnections(mobileState = false, wifiState = true)
        // When
        val connectionStates = simNetworkUtils.connectionsStates
        // Then
        verifyConnectionStates(
            connectionStates,
            mobileState = SimNetworkUtils.ConnectionState.DISCONNECTED,
            wifiState = SimNetworkUtils.ConnectionState.CONNECTED,
        )
    }

    @Test
    fun `getConnectionsStates networkCapabilities is null`() {
        // Given
        simNetworkUtils = SimNetworkUtilsImpl(context)
        every { connectivityManager.getNetworkCapabilities(any()) } returns null

        // When
        val connectionStates = simNetworkUtils.connectionsStates
        // Then
        verifyConnectionStates(
            connectionStates,
            mobileState = SimNetworkUtils.ConnectionState.DISCONNECTED,
            wifiState = SimNetworkUtils.ConnectionState.DISCONNECTED,
        )
    }

    private fun verifyConnectionStates(
        connectionStates: List<SimNetworkUtils.Connection>,
        mobileState: SimNetworkUtils.ConnectionState,
        wifiState: SimNetworkUtils.ConnectionState,
    ) {
        Truth.assertThat(connectionStates.size).isEqualTo(2)
        Truth.assertThat(connectionStates.first().state).isEqualTo(mobileState)
        Truth.assertThat(connectionStates.last().state).isEqualTo(wifiState)
    }

    private fun buildConnections(
        mobileState: Boolean,
        wifiState: Boolean,
    ) {
        networkCapabilities.apply {
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns mobileState
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns wifiState
        }
        simNetworkUtils = SimNetworkUtilsImpl(context)
    }
}
