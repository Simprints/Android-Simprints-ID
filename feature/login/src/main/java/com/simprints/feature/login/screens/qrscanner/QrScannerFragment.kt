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
import com.simprints.core.tools.extensions.hasPermission
import com.simprints.feature.login.R
import com.simprints.feature.login.databinding.FragmentQrScannerBinding
import com.simprints.infra.camera.CameraFrameProvider
import com.simprints.infra.camera.postprocess.DetectQrCodeUseCase
import com.simprints.infra.camera.usecase.GetBoundsRelativeToParentUseCase
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.awaitLayout
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class QrScannerFragment : Fragment(R.layout.fragment_qr_scanner) {
    private val binding by viewBinding(FragmentQrScannerBinding::bind)
    private val crashReportTag = LoggingConstants.CrashReportTag.LOGIN

    @Inject
    lateinit var cameraFrameProvider: CameraFrameProvider

    @Inject
    lateinit var getBoundsRelativeToParentUseCase: GetBoundsRelativeToParentUseCase

    @Inject
    lateinit var detectQrCodeUseCaseFactory: DetectQrCodeUseCase.Factory
    private val detectQrCodeUseCase: DetectQrCodeUseCase by lazy {
        detectQrCodeUseCaseFactory.create(crashReportTag = crashReportTag)
    }

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
        applySystemBarInsets(view)

        startAnalyzer()
        if (requireActivity().hasPermission(CAMERA)) {
            startCamera()
        } else {
            launchPermissionRequest.launch(CAMERA)
        }
    }

    override fun onDestroyView() {
        cameraFrameProvider.release()
        super.onDestroyView()
    }

    private fun startAnalyzer() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            cameraFrameProvider.frames
                .catch { e ->
                    Simber.e("Camera not available for QR scanning", e, tag = crashReportTag)
                    finishWithError(QrScannerResult.QrScannerError.CameraNotAvailable)
                }.collect { frame ->
                    detectQrCodeUseCase(frame)?.takeIf { it.isNotEmpty() }?.let { qrCode -> finishWithContent(qrCode) }
                }
        }
    }

    private fun startCamera() = viewLifecycleOwner.lifecycleScope.launch {
        // Wait for the views to be properly laid out
        binding.qrScannerPreview.awaitLayout()
        binding.qrScannerArea.awaitLayout()

        val targetRect = getBoundsRelativeToParentUseCase(
            parent = binding.qrScannerPreview,
            child = binding.qrScannerArea,
        )
        cameraFrameProvider.initialiseCamera(
            lifecycleOwner = viewLifecycleOwner,
            previewView = binding.qrScannerPreview,
            target = targetRect,
        ) {
            finishWithError(QrScannerResult.QrScannerError.CameraNotAvailable)
        }
        binding.qrScannerArea.isVisible = true
    }

    private fun finishWithContent(content: String) {
        findNavController().finishWithResult(this, QrScannerResult(content, null))
    }

    private fun finishWithError(error: QrScannerResult.QrScannerError) {
        findNavController().finishWithResult(this, QrScannerResult(null, error))
    }
}
