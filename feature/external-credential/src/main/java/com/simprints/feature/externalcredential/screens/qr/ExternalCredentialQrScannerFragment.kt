package com.simprints.feature.externalcredential.screens.qr

import android.Manifest.permission.CAMERA
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics.Event.LOGIN
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialQrScannerBinding
import com.simprints.feature.externalcredential.model.ExternalCredentialValidation
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.camera.qrscan.CameraHelper
import com.simprints.infra.uibase.camera.qrscan.QrCodeAnalyzer
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ExternalCredentialQrScannerFragment : Fragment(R.layout.fragment_external_credential_qr_scanner) {
    private val viewModel: ExternalCredentialViewModel by activityViewModels()
    private val binding by viewBinding(FragmentExternalCredentialQrScannerBinding::bind)

    @Inject
    lateinit var cameraHelper: CameraHelper

    @Inject
    lateinit var qrCodeAnalyzer: QrCodeAnalyzer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                qrCodeAnalyzer.scannedCode
                    .catch { e ->
                        Simber.e("Camera not available for QR scanning", e, tag = LOGIN)
                        // TODO [MS-964] Handle error properly
                        Toast.makeText(requireActivity(), "No camera", Toast.LENGTH_LONG).show()
                    }.collectLatest { qrCode ->
                        processQrCodeScan(qrCode)
                    }
            }
        }

        viewModel.externalCredentialResultDetails.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigateSafely(
                    this,
                    ExternalCredentialQrScannerFragmentDirections.actionExternalCredentialQrScannerToExternalCredentialQrConfirmation(
                        externalCredentialConfirmationResult = ExternalCredentialValidation(it.credential, it.result)
                    ),
                )
            }
        }

        if (requireActivity().hasPermission(CAMERA)) {
            binding.qrScannerPreview.post {
                startCamera()
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.recapture()
        }
    }

    private fun processQrCodeScan(qrCode: String?) = with(binding) {
        when (qrCode.isNullOrEmpty()) {
            true -> {
                qrPreviewCard.isVisible = false
                buttonScan.text = "No QR code detected"
                buttonScan.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.simprints.infra.resources.R.color.simprints_off_white
                    )
                )
                buttonScan.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.simprints.infra.resources.R.color.simprints_text_grey_light
                    )
                )
                buttonScan.setOnClickListener {}
            }

            false -> {
                qrPreviewCard.isVisible = true
                qrPreviewText.text = qrCode
                buttonScan.text = "CONTINUE"
                buttonScan.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.simprints.infra.resources.R.color.simprints_blue
                    )
                )
                buttonScan.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.simprints.infra.resources.R.color.simprints_text_white
                    )
                )
                buttonScan.setOnClickListener { viewModel.validateExternalCredential(credentialId = qrCode) }
            }
        }
    }

    private fun startCamera() {
        binding.qrScannerAim.isVisible = true
        qrCodeAnalyzer.getOrientation = { resources.configuration.orientation }
        qrCodeAnalyzer.getViewSize = {
            if (binding.qrScannerPreview.width == 0 || binding.qrScannerPreview.height == 0) {
                null to null
            } else {
                binding.qrScannerPreview.width to binding.qrScannerPreview.height
            }
        }
        qrCodeAnalyzer.getRect = {
            val isDrawn = binding.qrScannerAim.isShown &&
                binding.qrScannerAim.width > 0 &&
                binding.qrScannerAim.height > 0
            if (isDrawn) {
                Rect(
                    binding.qrScannerAim.left,
                    binding.qrScannerAim.top,
                    binding.qrScannerAim.right,
                    binding.qrScannerAim.bottom
                )
            } else null
        }

        cameraHelper.startCamera(
            viewLifecycleOwner,
            binding.qrScannerPreview,
            qrCodeAnalyzer,
        ) {
            // On error
            // TODO [MS-964] Handle error properly
            Toast.makeText(requireActivity(), "No Camera available", Toast.LENGTH_LONG).show()
        }
    }

}
