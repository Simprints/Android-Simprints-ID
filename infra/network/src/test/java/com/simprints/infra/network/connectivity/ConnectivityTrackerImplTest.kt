package com.simprints.infra.network.connectivity

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class ConnectivityTrackerImplTest {
    @MockK
    lateinit var connectivityManagerWrapper: ConnectivityManagerWrapper

    lateinit var connectivityTracker: ConnectivityTrackerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        connectivityTracker = ConnectivityTrackerImpl(connectivityManagerWrapper)
    }

    @Test
    fun `returns same live data to observe`() {
        val data1 = connectivityTracker.observeIsConnected()
        val data2 = connectivityTracker.observeIsConnected()

        assertThat(data1).isEqualTo(data2)
    }

    @Test
    fun `redirects connection request to connectivity manager`() {
        every { connectivityManagerWrapper.isNetworkAvailable() } returns true

        assertThat(connectivityTracker.isConnected()).isTrue()
        verify { connectivityManagerWrapper.isNetworkAvailable() }
    }
}
