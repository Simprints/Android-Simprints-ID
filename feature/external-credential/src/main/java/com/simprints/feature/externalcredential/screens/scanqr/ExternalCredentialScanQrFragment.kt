package com.simprints.feature.externalcredential.screens.scanqr

import android.Manifest.permission.CAMERA
import android.app.Dialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.view.View
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
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.tools.extentions.getCurrentPermissionStatus
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialScanQrBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.camera.qrscan.CameraHelper
import com.simprints.infra.uibase.camera.qrscan.QrCodeAnalyzer
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
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
    private val crashReportTag = MULTI_FACTOR_ID
    private val mainViewModel: ExternalCredentialViewModel by activityViewModels()
    private val viewModel by viewModels<ExternalCredentialScanQrViewModel>()

    private var dialog: Dialog? = null
    private var isCameraInitialized = false

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
        val cameraPermissionStatus = requireActivity().permissionFromResult(CAMERA, granted)
        viewModel.updateCameraPermissionStatus(cameraPermissionStatus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ExternalCredentialScanQrFragment started", tag = MULTI_FACTOR_ID)


        initObservers()

        if (!requireActivity().hasPermission(CAMERA)) {
            launchPermissionRequest.launch(CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        val cameraPermissionStatus = requireActivity().getCurrentPermissionStatus(CAMERA)
        viewModel.updateCameraPermissionStatus(cameraPermissionStatus)
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
                ScanQrState.ReadyToScan -> {
                    renderInitialState()
                    initCamera()
                }

                is ScanQrState.QrCodeCaptured -> renderScanComplete(state)
                is ScanQrState.NoCameraPermission -> renderNoPermission(state.shouldOpenPhoneSettings)
            }
        }
    }

    private fun renderInitialState() = with(binding) {
        permissionRequestView.isVisible = false
        qrInstructionsText.isVisible = true
        qrInstructionsText.text = getString(IDR.string.mfid_scan_instructions, getString(IDR.string.mfid_type_qr_code))
        qrPreviewCard.isVisible = false
        buttonScan.setText(IDR.string.mfid_qr_scan_no_qr_detected)
        buttonScan.isVisible = true
        buttonScan.isEnabled = false
        buttonScan.setOnClickListener {}
    }

    private fun renderScanComplete(state: ScanQrState.QrCodeCaptured) = with(binding) {
        val qrCodeValue = state.qrCodeValue
        qrInstructionsText.isVisible = false
        qrPreviewCard.isVisible = true
        qrPreviewText.text = state.qrCodeValue
        buttonScan.setText(IDR.string.mfid_continue)
        buttonScan.isEnabled = true
        buttonScan.setOnClickListener {
            if (viewModel.isValidQrCodeFormat(qrCodeValue)) {
                val args = ScannedCredential(
                    credential = qrCodeValue,
                    credentialType = ExternalCredentialType.QRCode,
                    documentImagePath = null,
                    credentialBoundingBox = null,
                    zoomedCredentialImagePath = null,
                )
                findNavController().navigateSafely(
                    this@ExternalCredentialScanQrFragment,
                    ExternalCredentialScanQrFragmentDirections.actionExternalCredentialSelectScanQrToExternalCredentialSearch(args)
                )
            } else {
                showInvalidQrCodeFormatDialog(
                    qrCodeValue = qrCodeValue,
                    onDismiss = {
                        dismissDialog()
                        viewModel.updateCapturedValue(null)
                    }
                )
            }
        }
    }

    private fun showInvalidQrCodeFormatDialog(
        qrCodeValue: String,
        onDismiss: () -> Unit,
    ) {
        dismissDialog()
        dialog = BottomSheetDialog(requireContext()).also {
            val view = layoutInflater.inflate(R.layout.dialog_qr_wrong_value, null)
                .also { view ->
                    val qrValueTextView = view.findViewById<TextView>(R.id.qrValue)
                    val buttonRescan = view.findViewById<Button>(R.id.buttonRescan)
                    qrValueTextView.text = qrCodeValue
                    buttonRescan.setOnClickListener { dismissDialog() }
                }
            it.setContentView(view)
            it.setOnDismissListener { onDismiss() }
            it.setCancelable(true)
            it.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            it.behavior.isDraggable = false
        }
        dialog?.show()
    }

    private fun initCamera() {
        binding.qrScannerArea.post {
            if (isCameraInitialized) return@post
            isCameraInitialized = true
            startCamera()
            startAnalyzer()
        }
    }

    private fun startAnalyzer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                qrCodeAnalyzer.scannedCode
                    .catch { e ->
                        Simber.e("Camera not available for QR scanning", e, tag = crashReportTag)
                        Toast.makeText(requireActivity(), "No camera", Toast.LENGTH_LONG).show()
                    }.collectLatest { qrCode ->
                        viewModel.updateCapturedValue(qrCode)
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
            Toast.makeText(requireActivity(), "No Camera available", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCropConfig(): QrCodeAnalyzer.CropConfig = with(binding) {
        val qrScannerArea = Rect(
            qrScannerArea.left,
            qrScannerArea.top,
            qrScannerArea.right,
            qrScannerArea.bottom
        )
        val orientation = resources.configuration.orientation
        val previewWidth = qrScannerPreview.width
        val previewHeight = qrScannerPreview.height
        return QrCodeAnalyzer.CropConfig(
            rect = qrScannerArea,
            orientation = orientation,
            rootViewWidth = previewWidth,
            rootViewHeight = previewHeight
        )
    }

    private fun renderNoPermission(shouldOpenPhoneSettings: Boolean) = with(binding) {
        qrInstructionsText.isVisible = false
        buttonScan.isVisible = false
        val bodyText = getString(IDR.string.mfid_scan_camera_permission_body, getString(IDR.string.mfid_type_qr_code))
        if (shouldOpenPhoneSettings) {
            permissionRequestView.init(
                title = IDR.string.face_capture_permission_denied,
                body = bodyText,
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
                body = bodyText,
                buttonText = IDR.string.face_capture_permission_action,
                onClickListener = {
                    launchPermissionRequest.launch(CAMERA)
                }
            )
        }
        permissionRequestView.isVisible = true
    }
}
