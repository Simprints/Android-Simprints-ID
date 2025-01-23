package com.simprints.feature.login.screens.qrscanner

import android.Manifest.permission.CAMERA
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics.Event.LOGIN
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.feature.login.R
import com.simprints.feature.login.databinding.FragmentQrScannerBinding
import com.simprints.feature.login.tools.camera.CameraHelper
import com.simprints.feature.login.tools.camera.QrCodeAnalyzer
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class QrScannerFragment : Fragment(R.layout.fragment_qr_scanner) {
    private val binding by viewBinding(FragmentQrScannerBinding::bind)

    @Inject
    lateinit var cameraHelper: CameraHelper

    @Inject
    lateinit var qrCodeAnalyzer: QrCodeAnalyzer

    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            finishWithError(QrScannerResult.QrScannerError.NoPermission)
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                qrCodeAnalyzer.scannedCode
                    .catch { e ->
                        Simber.e("Camera not available for QR scanning", e, tag = LOGIN)
                        finishWithError(QrScannerResult.QrScannerError.CameraNotAvailable)
                    }.collectLatest { qrCode ->
                        if (qrCode.isNotEmpty()) {
                            finishWithContent(qrCode)
                        }
                    }
            }
        }

        if (requireActivity().hasPermission(CAMERA)) {
            startCamera()
        } else {
            launchPermissionRequest.launch(CAMERA)
        }
    }

    private fun startCamera() {
        binding.qrScannerAim.isVisible = true
        cameraHelper.startCamera(
            viewLifecycleOwner,
            binding.qrScannerPreview,
            qrCodeAnalyzer,
        ) {
            finishWithError(QrScannerResult.QrScannerError.CameraNotAvailable)
        }
    }

    private fun finishWithContent(content: String) {
        findNavController().finishWithResult(this, QrScannerResult(content, null))
    }

    private fun finishWithError(error: QrScannerResult.QrScannerError) {
        findNavController().finishWithResult(this, QrScannerResult(null, error))
    }
}
