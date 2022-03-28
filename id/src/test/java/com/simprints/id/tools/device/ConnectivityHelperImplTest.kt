package com.simprints.id.tools.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

internal class ConnectivityHelperImplTest {

    @MockK
    lateinit var context: Context
    private lateinit var connectivityHelper: ConnectivityHelper
    private var mockedNetworkCapabilities: NetworkCapabilities? = null

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `test isNetworkAvailable should be false if capabilities is null`() {
        //Given
        val networkCapabilities = null
        setupNetworkCapabilities(networkCapabilities)
        //When
        val actual = connectivityHelper.isNetworkAvailable()
        //Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable should be false if network can't reach internet`() {
        //Given
        setupNetworkCapabilities()
        every { mockedNetworkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET) } returns false
        //When
        val actual = connectivityHelper.isNetworkAvailable()
        //Then
        assertThat(actual).isEqualTo(false)
    }

    @Test
    fun `test isNetworkAvailable should be false if network connection not yet validated`() {
        //Given
        setupNetworkCapabilities()
        mockedNetworkCapabilities?.apply {
            every { hasCapability(NET_CAPABILITY_INTERNET) } returns true
            every { hasCapability(NET_CAPABILITY_VALIDATED) } returns false
        }
        //When
        val actual = connectivityHelper.isNetworkAvailable()
        //Then
        assertThat(actual).isEqualTo(false)
    }
    @Test
    fun `test isNetworkAvailable success`() {
        //Given
        setupNetworkCapabilities()
        mockedNetworkCapabilities?.apply {
            every { hasCapability(NET_CAPABILITY_INTERNET) } returns true
            every { hasCapability(NET_CAPABILITY_VALIDATED) } returns true
        }
        //When
        val actual = connectivityHelper.isNetworkAvailable()
        //Then
        assertThat(actual).isEqualTo(true)
    }

    private fun setupNetworkCapabilities(networkCapabilities: NetworkCapabilities? = mockk()) {
        mockedNetworkCapabilities = networkCapabilities
        val connectivityManager: ConnectivityManager = mockk()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.getNetworkCapabilities(any()) } returns mockedNetworkCapabilities
        connectivityHelper = ConnectivityHelperImpl(context)
    }
}
