package com.simprints.feature.externalcredential.screens.confirmation

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.extentions.nullIfEmpty
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialQrConfirmationBinding
import com.simprints.feature.externalcredential.model.ExternalCredentialResult
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ExternalCredentialQrConfirmationFragment : Fragment(R.layout.fragment_external_credential_qr_confirmation) {
    private val viewModel: ExternalCredentialViewModel by activityViewModels()
    private val binding by viewBinding(FragmentExternalCredentialQrConfirmationBinding::bind)
    private val args: ExternalCredentialQrConfirmationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding) {
        val result = args.externalCredentialConfirmationResult
        when (result.result) {
            ExternalCredentialResult.ENROL_OK -> {
                externalCredentialSmallIcon.isVisible = true
                externalCredentialLargeIcon.setImageResource(R.drawable.onboarding_straight)
                externalCredentialLargeIcon.setColorFilter(ContextCompat.getColor(requireContext(), IDR.color.simprints_black))
                externalCredentialStatusTitle.text = "QR Code linked to subject"
            }

            ExternalCredentialResult.ENROL_DUPLICATE_FOUND -> {
                externalCredentialSmallIcon.isVisible = false
                confirmationBtn.isVisible = false
                externalCredentialSubjectId.isVisible = false
                externalCredentialLargeIcon.setImageResource(R.drawable.ic_warning)
                // [MS-964] For some reason this is the only resolved way to make the icon red
                externalCredentialLargeIcon.setColorFilter(ContextCompat.getColor(requireContext(), IDR.color.simprints_red_dark))
                externalCredentialStatusTitle.text = "This QR code is already linked to another subject ID"
            }
            ExternalCredentialResult.SEARCH_FOUND -> {
                externalCredentialSmallIcon.isVisible = true
                externalCredentialLargeIcon.setImageResource(R.drawable.onboarding_straight)
                externalCredentialLargeIcon.setColorFilter(ContextCompat.getColor(requireContext(), IDR.color.simprints_black))
                externalCredentialStatusTitle.text = "Subject found"
            }
            ExternalCredentialResult.SEARCH_NOT_FOUND -> {
                externalCredentialSmallIcon.isVisible = false
                externalCredentialSubjectId.isVisible = false
                externalCredentialLargeIcon.setImageResource(R.drawable.ic_warning)
                // [MS-964] For some reason this is the only resolved way to make the icon red
                externalCredentialLargeIcon.setColorFilter(ContextCompat.getColor(requireContext(), IDR.color.simprints_red_dark))
                externalCredentialStatusTitle.text = "No subject linked to QR code"
                confirmationBtn.text = "Search 1:N"
            }

            ExternalCredentialResult.CREDENTIAL_EMPTY -> {
                externalCredentialSmallIcon.isVisible = false
                confirmationBtn.isVisible = false
                externalCredentialSubjectId.isVisible = false
                externalCredentialLargeIcon.setImageResource(R.drawable.ic_warning)
                // [MS-964] For some reason this is the only resolved way to make the icon red
                externalCredentialLargeIcon.setColorFilter(ContextCompat.getColor(requireContext(), IDR.color.simprints_red_dark))
                externalCredentialStatusTitle.text = "Cannot process QR code data"
            }
        }

        externalCredentialBody.text = "QR data: ${result.credential.data.nullIfEmpty() ?: "???"}"
        externalCredentialSubjectId.text = "SubjectId: ${result.credential.subjectId}"

        binding.confirmationBtn.setOnClickListener {
            val confirmationResult = args.externalCredentialConfirmationResult
            // [MS-985] The imagePath for QR code is null because we don't currently store the scanned image
            viewModel.confirmAndFinishFlow(credential = confirmationResult.credential, imagePath = null)
        }

        binding.recaptureBtn.setOnClickListener {
            viewModel.recapture()
        }
    }

}


