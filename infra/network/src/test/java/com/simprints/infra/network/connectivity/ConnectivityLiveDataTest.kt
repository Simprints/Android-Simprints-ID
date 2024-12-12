package com.simprints.infra.network.connectivity

import android.net.ConnectivityManager.NetworkCallback
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ConnectivityLiveDataTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var connectivityManagerWrapper: ConnectivityManagerWrapper

    private var networkCallback = CapturingSlot<NetworkCallback>()

    private lateinit var connectivityLiveData: ConnectivityLiveData

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { connectivityManagerWrapper.registerNetworkCallback(capture(networkCallback)) } returns Unit

        connectivityLiveData = ConnectivityLiveData(connectivityManagerWrapper)
    }

    @Test
    fun `live data registers and unregisters callback`() {
        every { connectivityManagerWrapper.isNetworkAvailable() } returns true

        val testObserver = connectivityLiveData.test()
        connectivityLiveData.removeObserver(testObserver)

        verify { connectivityManagerWrapper.registerNetworkCallback(any()) }
        verify { connectivityManagerWrapper.unregisterNetworkCallback(any()) }
    }

    @Test
    fun `live data initialises with current value`() {
        every { connectivityManagerWrapper.isNetworkAvailable() } returns true

        val testObserver = connectivityLiveData.test()

        verify { connectivityManagerWrapper.isNetworkAvailable() }
        assertThat(testObserver.valueHistory()).isEqualTo(listOf(true))
    }

    @Test
    fun `live data notified on connection available`() {
        every { connectivityManagerWrapper.isNetworkAvailable() } returns false

        val testObserver = connectivityLiveData.test()
        networkCallback.captured.onAvailable(mockk())

        assertThat(testObserver.valueHistory()).isEqualTo(listOf(false, true))
    }

    @Test
    fun `live data notified on connection lost`() {
        every { connectivityManagerWrapper.isNetworkAvailable() } returns true

        val testObserver = connectivityLiveData.test()
        networkCallback.captured.onLost(mockk())

        assertThat(testObserver.valueHistory()).isEqualTo(listOf(true, false))
    }

    @Test
    fun `live data notified on connection unavailable`() {
        every { connectivityManagerWrapper.isNetworkAvailable() } returns true

        val testObserver = connectivityLiveData.test()
        networkCallback.captured.onUnavailable()

        assertThat(testObserver.valueHistory()).isEqualTo(listOf(true, false))
    }
}
