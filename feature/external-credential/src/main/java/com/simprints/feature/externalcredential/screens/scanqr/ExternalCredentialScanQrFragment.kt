package com.simprints.feature.externalcredential.screens.scanqr

import android.Manifest
import android.Manifest.permission.CAMERA
import android.app.Dialog
import android.content.Intent
import android.graphics.Rect
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialScanQrBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.infra.logging.LoggingConstants
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
import kotlin.getValue
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ExternalCredentialScanQrFragment : Fragment(R.layout.fragment_external_credential_scan_qr) {

    private val binding by viewBinding(FragmentExternalCredentialScanQrBinding::bind)
    private val crashReportTag = LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
    private val mainViewModel: ExternalCredentialViewModel by activityViewModels()
    private val viewModel by viewModels<ExternalCredentialScanQrViewModel>()

    private var dialog: Dialog? = null
    private var shouldCheckCameraPermission = true

    @Inject
    lateinit var cameraHelperFactory: CameraHelper.Factory
    private val cameraHelper: CameraHelper by lazy {
        cameraHelperFactory.create(crashReportTag)
    }

    @Inject
    lateinit var qrCodeAnalyzerFactory: QrCodeAnalyzer.Factory
    private lateinit var qrCodeAnalyzer: QrCodeAnalyzer

    private val launchPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val permissionStatus = requireActivity().permissionFromResult(CAMERA, granted)
        shouldCheckCameraPermission = permissionStatus == PermissionStatus.DeniedNeverAskAgain
        when (permissionStatus) {
            PermissionStatus.Granted -> {
                initObservers()
                startCamera()
                startAnalyzer()
            }

            PermissionStatus.Denied -> renderNoPermission(shouldOpenSettings = false)
            PermissionStatus.DeniedNeverAskAgain -> renderNoPermission(shouldOpenSettings = true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldCheckCameraPermission) {
            // Check permission in onResume() so that if user left the app to go to Settings
            // and give the permission, it's reflected when they come back to SID
            launchPermissionRequest.launch(CAMERA)
        }
    }

    override fun onDestroy() {
        dismissDialog()
        super.onDestroy()
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun initObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                ScanQrState.NothingScanned -> renderInitialState()
                is ScanQrState.ScanComplete -> renderScanComplete(state)
            }
        }
    }

    private fun renderInitialState() = with(binding) {
        permissionRequestView.isVisible = false
        qrPreviewCard.isVisible = false
        buttonScan.setText(IDR.string.mfid_qr_scan_no_qr_detected)
        buttonScan.isVisible = true
        buttonScan.isEnabled = false
        buttonScan.setOnClickListener {}
    }

    private fun renderScanComplete(state: ScanQrState.ScanComplete) = with(binding) {
        val qrCodeValue = state.qrCodeValue
        qrPreviewCard.isVisible = true
        qrPreviewText.text = state.qrCodeValue
        buttonScan.setText(IDR.string.mfid_continue)
        buttonScan.isEnabled = true
        buttonScan.setOnClickListener {
            if (viewModel.isValidQrCodeFormat(qrCodeValue)) {
                mainViewModel.setExternalCredentialValue(qrCodeValue)
                findNavController().navigateSafely(
                    this@ExternalCredentialScanQrFragment,
                    R.id.action_externalCredentialSelectScanQr_to_externalCredentialSearch
                )
            } else {
                showInvalidQrCodeFormatDialog(
                    qrCodeValue = qrCodeValue,
                    onRescan = {
                        dismissDialog()
                    }
                )
            }
        }
    }

    private fun showInvalidQrCodeFormatDialog(
        qrCodeValue: String,
        onRescan: () -> Unit,
    ) {
        dismissDialog()
        dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_qr_wrong_value, null)
        val qrValueTextView = view.findViewById<TextView>(R.id.qrValue)
        val buttonRescan = view.findViewById<Button>(R.id.buttonRescan)

        qrValueTextView.text = qrCodeValue

        buttonRescan.setOnClickListener { onRescan() }

        dialog?.setContentView(view)
        dialog?.setCancelable(true)
        dialog?.show()
        (dialog as? BottomSheetDialog)?.apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
    }

    private fun startAnalyzer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                qrCodeAnalyzer.scannedCode
                    .catch { e ->
                        Simber.e("Camera not available for QR scanning", e, tag = crashReportTag)
                        // TODO [MS-1165] Handle camera permissions
                        Toast.makeText(requireActivity(), "No camera", Toast.LENGTH_LONG).show()
                    }.collectLatest { qrCode ->
                        viewModel.setQrCodeValue(qrCode)
                    }
            }
        }
    }

    private fun startCamera() {
        val cropConfig = getCropConfig()
        qrCodeAnalyzer = qrCodeAnalyzerFactory.create(cropConfig = cropConfig, crashReportTag = crashReportTag)

        cameraHelper.startCamera(
            viewLifecycleOwner,
            binding.qrScannerPreview,
            qrCodeAnalyzer,
        ) {
            // On error
            // TODO [MS-1165] Handle lack of camera permissions
            Toast.makeText(requireActivity(), "No Camera available", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCropConfig(): QrCodeAnalyzer.CropConfig {
        val qrScannerArea = Rect(
            binding.qrScannerAim.left,
            binding.qrScannerAim.top,
            binding.qrScannerAim.right,
            binding.qrScannerAim.bottom
        )
        val orientation = resources.configuration.orientation
        val previewWidth = binding.qrScannerPreview.width
        val previewHeight = binding.qrScannerPreview.height
        return QrCodeAnalyzer.CropConfig(
            rect = qrScannerArea,
            orientation = orientation,
            rootViewWidth = previewWidth,
            rootViewHeight = previewHeight
        )
    }

    private fun renderNoPermission(shouldOpenSettings: Boolean) {
        binding.apply {
            buttonScan.isVisible = false
            if (shouldOpenSettings) {
                permissionRequestView.init(
                    title = IDR.string.face_capture_permission_denied,
                    body = IDR.string.login_qr_code_scanning_camera_permission_error,
                    buttonText = IDR.string.fingerprint_connect_phone_settings_button,
                    onClickListener = {
                        requireActivity().startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:${requireActivity().packageName}".toUri(),
                            ),
                        )
                    }
                )
            } else {
                permissionRequestView.init(
                    title = IDR.string.face_capture_permission_denied,
                    body = IDR.string.login_qr_code_scanning_camera_permission_error,
                    buttonText = IDR.string.face_capture_permission_action,
                    onClickListener = {
                        launchPermissionRequest.launch(Manifest.permission.CAMERA)
                    }
                )
            }
            permissionRequestView.isVisible = true
        }
    }
}
