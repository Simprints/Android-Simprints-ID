package com.simprints.feature.externalcredential.screens.qr

import android.Manifest.permission.CAMERA
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.tools.camera.CameraHelper
import com.simprints.feature.externalcredential.tools.camera.QrCodeAnalyzer
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
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
                        if (qrCode.isNotEmpty()) {
                            viewModel.processExternalCredential(data = qrCode)
                        }
                    }
            }
        }

        if (requireActivity().hasPermission(CAMERA)) {
            startCamera()
        }
    }

    private fun startCamera() {
        binding.qrScannerAim.isVisible = true
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
