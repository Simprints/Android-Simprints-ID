package com.simprints.infra.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

internal class ConnectivityManagerWrapperImplTest {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var connectivityManager: ConnectivityManager

    @MockK
    lateinit var networkCapabilities: NetworkCapabilities

    private lateinit var connectivityManagerWrapper: ConnectivityManagerWrapper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `test isNetworkAvailable should be false if capabilities is null`() {
        // Given
        setupNetworkCapabilities(null)
        // When
        val actual = connectivityManagerWrapper.isNetworkAvailable()
        // Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable should be false if network can't reach internet`() {
        // Given
        every { networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET) } returns false
        setupNetworkCapabilities(networkCapabilities)
        // When
        val actual = connectivityManagerWrapper.isNetworkAvailable()
        // Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable should be false if network connection not yet validated`() {
        // Given
        every { networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NET_CAPABILITY_VALIDATED) } returns false
        setupNetworkCapabilities(networkCapabilities)
        // When
        val actual = connectivityManagerWrapper.isNetworkAvailable()
        // Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable success`() {
        // Given
        every { networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NET_CAPABILITY_VALIDATED) } returns true
        setupNetworkCapabilities(networkCapabilities)
        // When
        val actual = connectivityManagerWrapper.isNetworkAvailable()
        // Then
        assertThat(actual).isEqualTo(true)
    }

    private fun setupNetworkCapabilities(mockedNetworkCapabilities: NetworkCapabilities?) {
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.getNetworkCapabilities(any()) } returns mockedNetworkCapabilities

        connectivityManagerWrapper = ConnectivityManagerWrapper(context)
    }
}
