package com.simprints.feature.externalcredential.screens.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.resources.R as IDR
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExternalCredentialViewModelTest {

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

    @Test
    fun `setExternalCredentialValue updates state`() {
        val observer = viewModel.stateLiveData.test()
        val value = "value"
        viewModel.setExternalCredentialValue(value)
        assertThat(observer.value()?.credentialValue).isEqualTo(value)
    }

    @Test
    fun `init sets state only once`() {
        val observer = viewModel.stateLiveData.test()
        val subjectId = "subjectId"
        val flowType = FlowType.IDENTIFY
        val params = ExternalCredentialParams(subjectId = subjectId, flowType = flowType)
        val paramsSecond = ExternalCredentialParams(subjectId = "other", flowType = FlowType.VERIFY)

        viewModel.init(params)
        val firstState = observer.value()

        viewModel.init(paramsSecond)

        assertThat(observer.value()).isEqualTo(firstState)
        assertThat(observer.value()?.subjectId).isEqualTo(subjectId)
        assertThat(observer.value()?.flowType).isEqualTo(flowType)
    }

}
