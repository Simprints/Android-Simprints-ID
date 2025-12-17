package com.simprints.infra.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
internal class ConnectivityManagerWrapperImplTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var connectivityManager: ConnectivityManager

    @MockK
    lateinit var networkCapabilities: NetworkCapabilities

    private var networkCallback = CapturingSlot<NetworkCallback>()

    private lateinit var connectivityManagerWrapper: ConnectivityManagerWrapper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { context.getSystemService(ConnectivityManager::class.java) } returns connectivityManager

        every { connectivityManager.getNetworkCapabilities(any()) } returns networkCapabilities
        every { connectivityManager.activeNetwork } returns mockk()
        every { connectivityManager.registerNetworkCallback(any(), capture(networkCallback)) } returns Unit

        connectivityManagerWrapper = ConnectivityManagerWrapper(context)
    }

    @Test
    fun `test isNetworkAvailable should be false if capabilities is null`() {
        // Given
        every { connectivityManager.getNetworkCapabilities(any()) } returns null

        // When
        val actual = connectivityManagerWrapper.isConnected()
        // Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable should be false if network can't reach internet`() {
        // Given
        every { networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET) } returns false
        // When
        val actual = connectivityManagerWrapper.isConnected()
        // Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable should be false if network connection not yet validated`() {
        // Given
        every { networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NET_CAPABILITY_VALIDATED) } returns false
        // When
        val actual = connectivityManagerWrapper.isConnected()
        // Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable success`() {
        // Given
        setCapabilities(internet = true, validated = true)
        // Then
        assertThat(connectivityManagerWrapper.isConnected()).isTrue()
    }

    @Test
    fun `connection flow registers and unregisters callback`() = runTest {
        setCapabilities(internet = true, validated = true)

        connectivityManagerWrapper.observeIsConnected().first()

        verify { connectivityManager.registerNetworkCallback(any(), any<NetworkCallback>()) }
        verify { connectivityManager.unregisterNetworkCallback(any<NetworkCallback>()) }
    }

    @Test
    fun `connection flow initialises with current value`() = runTest {
        setCapabilities(internet = true, validated = true)

        assertThat(connectivityManagerWrapper.observeIsConnected().first()).isTrue()
    }

    @Test
    fun `connection flow notified on connection available`() = runTest {
        setCapabilities(internet = false, validated = false)

        val collectedValues = mutableListOf<Boolean>()
        val collectJob = launch {
            connectivityManagerWrapper.observeIsConnected().onEach { collectedValues.add(it) }.collect()
        }
        advanceUntilIdle()

        networkCallback.captured.onAvailable(mockk())
        advanceUntilIdle()

        collectJob.cancel()
        advanceUntilIdle()

        assertThat(collectedValues).containsExactly(false, true)
    }

    @Test
    fun `connection flow notified on connection lost`() = runTest {
        setCapabilities(internet = true, validated = true)

        val collectedValues = mutableListOf<Boolean>()
        val collectJob = launch {
            connectivityManagerWrapper.observeIsConnected().onEach { collectedValues.add(it) }.collect()
        }
        advanceUntilIdle()

        networkCallback.captured.onLost(mockk())
        advanceUntilIdle()

        collectJob.cancel()
        advanceUntilIdle()

        assertThat(collectedValues).containsExactly(true, false)
    }

    @Test
    fun `connection flow notified on connection unavailable`() = runTest {
        setCapabilities(internet = true, validated = true)

        val collectedValues = mutableListOf<Boolean>()
        val collectJob = launch {
            connectivityManagerWrapper.observeIsConnected().onEach { collectedValues.add(it) }.collect()
        }
        advanceUntilIdle()

        networkCallback.captured.onUnavailable()
        advanceUntilIdle()

        collectJob.cancel()
        advanceUntilIdle()

        assertThat(collectedValues).containsExactly(true, false)
    }

    private fun setCapabilities(
        internet: Boolean,
        validated: Boolean,
    ) {
        every { networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET) } returns internet
        every { networkCapabilities.hasCapability(NET_CAPABILITY_VALIDATED) } returns validated
    }
}
