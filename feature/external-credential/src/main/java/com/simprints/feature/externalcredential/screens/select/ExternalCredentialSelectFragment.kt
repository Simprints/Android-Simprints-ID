package com.simprints.feature.externalcredential.screens.select

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSelectBinding
import com.simprints.feature.externalcredential.ext.getCredentialTypeRes
import com.simprints.feature.externalcredential.ext.getQuantityCredentialString
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.asOcrDocumentType
import com.simprints.feature.externalcredential.screens.select.view.ExternalCredentialTypeAdapter
import com.simprints.feature.externalcredential.view.SkipScanConfirmationDialog
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
    private val binding by viewBinding(FragmentExternalCredentialSelectBinding::bind)

    private var dialog: Dialog? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ExternalCredentialSelectFragment started", tag = ORCHESTRATION)

        observeChanges()
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
            dismissDialog()
            dialog = SkipScanConfirmationDialog(
                context = requireContext(),
                credentialTypes = types,
                onConfirm = {
                    dismissDialog()
                    if (mainViewModel.defaultSkipReason != null) {
                        mainViewModel.bypassSkipScreen()
                    } else {
                        findNavController().navigateSafely(
                            this,
                            ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialSkip(),
                        )
                    }
                },
                onCancel = ::dismissDialog,
            ).also { it.show() }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            binding.skipScanning.performClick()
        }
    }

    private fun initViews(types: List<ExternalCredentialType>) {
        binding.title.text = resources.getQuantityCredentialString(
            id = IDR.string.mfid_scan_action,
            specificCredentialRes = resources.getCredentialTypeRes(types.firstOrNull()),
            multipleCredentialsRes = IDR.string.mfid_type_any_document,
            credentialTypes = types,
        )
    }

    private fun observeChanges() {
        mainViewModel.externalCredentialTypes.observe(viewLifecycleOwner) { externalCredentialTypes ->
            updateSelectedCredentialType(null)
            fillRecyclerView(externalCredentialTypes)
            initViews(externalCredentialTypes)
            initListeners(externalCredentialTypes)
            mainViewModel.selectionStarted()
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
            ExternalCredentialType.GhanaIdCard,
            -> startOcr(type.asOcrDocumentType())

            ExternalCredentialType.QRCode -> startQrScan()
        }
    }

    private fun startQrScan() {
        findNavController().navigateSafely(
            this,
            ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialScanQr(),
        )
    }

    private fun startOcr(ocrDocumentType: OcrDocumentType) {
        findNavController().navigateSafely(
            this,
            ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialScanOcr(ocrDocumentType),
        )
    }
}
