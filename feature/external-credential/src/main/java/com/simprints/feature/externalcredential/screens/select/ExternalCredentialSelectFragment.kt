package com.simprints.feature.externalcredential.screens.select

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSelectBinding
import com.simprints.feature.externalcredential.ext.getCredentialTypeRes
import com.simprints.feature.externalcredential.ext.getQuantityCredentialString
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.mapToOcrDocumentType
import com.simprints.feature.externalcredential.screens.select.view.ExternalCredentialTypeAdapter
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ExternalCredentialSelectFragment : Fragment(R.layout.fragment_external_credential_select) {

    private val mainViewModel: ExternalCredentialViewModel by activityViewModels()
    private val viewModel by viewModels<ExternalCredentialSelectViewModel>()
    private val binding by viewBinding(FragmentExternalCredentialSelectBinding::bind)

    private var dialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ExternalCredentialSelectFragment started", tag = ORCHESTRATION)

        observeChanges()
        viewModel.loadExternalCredentials()
    }

    override fun onDestroy() {
        dismissDialog()
        super.onDestroy()
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun initListeners(types: List<ExternalCredentialType>) {
        binding.skipScanning.setOnClickListener {
            displaySkipScanningConfirmationDialog(
                credentialTypes = types,
                onConfirm = {
                    dismissDialog()
                    findNavController().navigateSafely(
                        this,
                        ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialSkip(),
                    )
                },
                onCancel = ::dismissDialog
            )
        }
    }

    private fun initViews(types: List<ExternalCredentialType>) {
        binding.title.text = resources.getQuantityCredentialString(
            id = IDR.plurals.mfid_scan_action,
            specificCredentialRes = resources.getCredentialTypeRes(types.firstOrNull()),
            multipleCredentialsRes = IDR.string.mfid_type_any_document,
            credentialTypes = types,
        )
    }

    private fun observeChanges() {
        viewModel.externalCredentialTypes.observe(viewLifecycleOwner) { externalCredentialTypes ->
            updateSelectedCredentialType(null)
            fillRecyclerView(externalCredentialTypes)
            initViews(externalCredentialTypes)
            initListeners(externalCredentialTypes)
        }
    }

    private fun fillRecyclerView(types: List<ExternalCredentialType>) {
        with(binding.documentsRecyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ExternalCredentialTypeAdapter(types) { selectedType ->
                updateSelectedCredentialType(selectedType)
                navigateToScanner(selectedType)
            }
        }
    }

    private fun updateSelectedCredentialType(type: ExternalCredentialType?) {
        mainViewModel.setSelectedExternalCredentialType(type)
    }

    private fun navigateToScanner(type: ExternalCredentialType) {
        when (type) {
            ExternalCredentialType.NHISCard,
            ExternalCredentialType.GhanaIdCard -> startOcr(type.mapToOcrDocumentType())

            ExternalCredentialType.QRCode -> startQrScan()
        }
    }

    private fun startQrScan() {
        findNavController().navigateSafely(
            this,
            ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialScanQr(),
        )
    }

    private fun displaySkipScanningConfirmationDialog(
        credentialTypes: List<ExternalCredentialType>,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        dismissDialog()
        dialog = BottomSheetDialog(requireContext()).also {
            val view = layoutInflater.inflate(R.layout.dialog_skip_scan_confirm, null)
                .also { view ->
                    val bodyText = view.findViewById<TextView>(R.id.skipDialogBodyText)
                    val cancelButton = view.findViewById<Button>(R.id.buttonCancel)
                    val confirmButton = view.findViewById<Button>(R.id.buttonSkip)

                    bodyText.text = resources.getQuantityCredentialString(
                        id = IDR.plurals.mfid_dialog_skip_scan_body,
                        credentialTypes = credentialTypes,
                        specificCredentialRes = resources.getCredentialTypeRes(credentialTypes.firstOrNull()),
                        multipleCredentialsRes = IDR.string.mfid_type_any_document
                    )

                    confirmButton.setOnClickListener { onConfirm() }
                    cancelButton.setOnClickListener { onCancel() }
                }
            it.setContentView(view)
            it.setCancelable(true)
            it.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            it.behavior.isDraggable = false
        }

        dialog?.show()
    }

    private fun startOcr(ocrDocumentType: OcrDocumentType) {
        findNavController().navigateSafely(
            this,
            ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialScanOcr(ocrDocumentType),
        )
    }

}
