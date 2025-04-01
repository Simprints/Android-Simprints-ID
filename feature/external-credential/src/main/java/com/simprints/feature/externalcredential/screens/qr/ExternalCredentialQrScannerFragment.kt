package com.simprints.feature.externalcredential.screens.qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialQrScannerBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
internal class ExternalCredentialQrScannerFragment : Fragment(R.layout.fragment_external_credential_qr_scanner) {
    private val viewModel: ExternalCredentialViewModel by activityViewModels()
    private val binding by viewBinding(FragmentExternalCredentialQrScannerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.tempQrText.setOnClickListener {
            viewModel.processExternalCredentialAndFinish(data = UUID.randomUUID().toString())
        }
    }
}
