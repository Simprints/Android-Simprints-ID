package com.simprints.feature.externalcredential.screens.select

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSelectBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ExternalCredentialSelectFragment : Fragment(R.layout.fragment_external_credential_select) {
    private val viewModel: ExternalCredentialViewModel by activityViewModels()
    private val binding by viewBinding(FragmentExternalCredentialSelectBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.scanQrCard.setOnClickListener {
            findNavController().navigateSafely(
                this,
                ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialQrScanner(),
            )
        }
    }
}
