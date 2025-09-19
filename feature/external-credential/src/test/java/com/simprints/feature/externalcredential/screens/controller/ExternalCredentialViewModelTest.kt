package com.simprints.feature.externalcredential.screens.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.resources.R as IDR
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ExternalCredentialViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: ExternalCredentialViewModel

    @Before
    fun setUp() {
        viewModel = ExternalCredentialViewModel()
    }

    @Test
    fun `initial state is EMPTY`() {
        val observer = viewModel.stateLiveData.test()
        assertThat(observer.value()).isEqualTo(ExternalCredentialState.EMPTY)
    }

    @Test
    fun `setSelectedExternalCredentialType updates state`() {
        val observer = viewModel.stateLiveData.test()

        viewModel.setSelectedExternalCredentialType(ExternalCredentialType.GhanaIdCard)

        assertThat(observer.value()?.selectedType).isEqualTo(ExternalCredentialType.GhanaIdCard)
    }

    @Test
    fun `setSelectedExternalCredentialType null resets state`() {
        val observer = viewModel.stateLiveData.test()

        viewModel.setSelectedExternalCredentialType(null)

        assertThat(observer.value()?.selectedType).isNull()
    }

    @Test
    fun `mapTypeToStringResource returns correct string ids`() {
        assertThat(viewModel.mapTypeToStringResource(ExternalCredentialType.NHISCard))
            .isEqualTo(IDR.string.mfid_type_nhis_card)
        assertThat(viewModel.mapTypeToStringResource(ExternalCredentialType.GhanaIdCard))
            .isEqualTo(IDR.string.mfid_type_ghana_id_card)
        assertThat(viewModel.mapTypeToStringResource(ExternalCredentialType.QRCode))
            .isEqualTo(IDR.string.mfid_type_qr_code)
    }
}
