package com.simprints.fingerprint.connect.screens.controller

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test


class ConnectScannerControllerViewModelTest {
    private lateinit var viewModel: ConnectScannerControllerViewModel

    @Before
    fun setUp() {
        viewModel = ConnectScannerControllerViewModel()
    }

    @Test
    fun `when isInitialized is set, it stores the correct value`() {
        val initialIsInitialized = false
        val expectedIsInitialized = true

        assertThat(viewModel.isInitialized).isEqualTo(initialIsInitialized)
        viewModel.isInitialized = expectedIsInitialized
        assertThat(viewModel.isInitialized).isEqualTo(expectedIsInitialized)
    }
}