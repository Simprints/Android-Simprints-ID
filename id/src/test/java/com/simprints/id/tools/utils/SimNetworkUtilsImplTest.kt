package com.simprints.id.tools.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.ConnectionState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

internal class SimNetworkUtilsImplTest {

    @MockK
    private lateinit var context: Context

    private var networkCapabilities: NetworkCapabilities? = null

    private lateinit var simNetworkUtils: SimNetworkUtils

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setUpNetworkCapabilities()

    }

    private fun setUpNetworkCapabilities() {
        networkCapabilities = mockk()
        val connectivityManager: ConnectivityManager = mockk()
        every { context.getSystemService(CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.getNetworkCapabilities(any()) } returns networkCapabilities
    }

    @Test
    fun `getConnectionsStates mobile connected and wifi not connected`() {
        //Given
        buildConnections(mobileState = true, wifiState = false)
        //When
        val connectionStates = simNetworkUtils.connectionsStates
        //Then
        verifyConnectionStates(
            connectionStates,
            mobileState = ConnectionState.CONNECTED,
            wifiState = ConnectionState.DISCONNECTED
        )
    }

    @Test
    fun `getConnectionsStates mobile not  connected and wifi  connected`() {
        //Given
        buildConnections(mobileState = false, wifiState = true)
        //When
        val connectionStates = simNetworkUtils.connectionsStates
        //Then
        verifyConnectionStates(
            connectionStates,
            mobileState = ConnectionState.DISCONNECTED,
            wifiState = ConnectionState.CONNECTED
        )
    }

    @Test
    fun `getConnectionsStates networkCapabilities is null`() {
        //Given
        simNetworkUtils = SimNetworkUtilsImpl(context)
        networkCapabilities = null
        //When
        val connectionStates = simNetworkUtils.connectionsStates
        //Then
        verifyConnectionStates(
            connectionStates,
            mobileState = ConnectionState.DISCONNECTED,
            wifiState = ConnectionState.DISCONNECTED
        )
    }

    private fun verifyConnectionStates(
        connectionStates: List<SimNetworkUtils.Connection>,
        mobileState: ConnectionState,
        wifiState: ConnectionState
    ) {
        assertThat(connectionStates.size).isEqualTo(2)
        assertThat(connectionStates.first().state).isEqualTo(mobileState)
        assertThat(connectionStates.last().state).isEqualTo(wifiState)

    }

    private fun buildConnections(mobileState: Boolean, wifiState: Boolean) {
        networkCapabilities?.apply {
            every { hasTransport(TRANSPORT_CELLULAR) } returns mobileState
            every { hasTransport(TRANSPORT_WIFI) } returns wifiState
        }
        simNetworkUtils = SimNetworkUtilsImpl(context)
    }
}
