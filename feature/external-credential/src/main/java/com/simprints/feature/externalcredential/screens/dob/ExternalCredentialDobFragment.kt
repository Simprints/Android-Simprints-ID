package com.simprints.feature.externalcredential.screens.dob

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialDobBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExternalCredentialDobFragment : Fragment(R.layout.fragment_external_credential_dob) {

    private val viewModel: ExternalCredentialViewModel by activityViewModels()
    private val binding by viewBinding(FragmentExternalCredentialDobBinding::bind)
    private val args: ExternalCredentialDobFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        initObservers()
    }

    private fun initObservers() {
        viewModel.externalCredentialResultDetails.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            viewModel.confirmAndFinishFlow(credential = it.credential, imagePath = null, addCredentialRightNow = true)
        }
    }

    private fun initListeners() {
        binding.externalCredentialEditText.doOnTextChanged { text, start, before, count ->
            val dob = text?.toString()?.trim() ?: ""
            val length = dob.length
            val isValid = dob.all { it.isDigit() || it == '/' }
            binding.buttonConfirm.isEnabled = length in 9..10 && isValid
        }

        binding.buttonConfirm.setOnClickListener {
            val dob = binding.externalCredentialEditText.text?.toString()?.trim() ?: ""
            if (dob.isEmpty()) return@setOnClickListener
            viewModel.validateExternalCredential(credentialId = dob)
        }
    }
}
