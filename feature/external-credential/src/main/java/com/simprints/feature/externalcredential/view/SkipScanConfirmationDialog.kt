package com.simprints.feature.externalcredential.view

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.databinding.DialogSkipScanConfirmBinding
import com.simprints.feature.externalcredential.ext.getCredentialTypeRes
import com.simprints.feature.externalcredential.ext.getQuantityCredentialString
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI class")
class SkipScanConfirmationDialog(
    context: Context,
    credentialTypes: List<ExternalCredentialType>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) : BottomSheetDialog(context) {
    private val binding = DialogSkipScanConfirmBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)
        setCancelable(true)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false

        binding.skipDialogBodyText.text = context.resources.getQuantityCredentialString(
            id = IDR.string.mfid_skip_scan_dialog_body,
            credentialTypes = credentialTypes,
            specificCredentialRes = context.resources.getCredentialTypeRes(credentialTypes.firstOrNull()),
            multipleCredentialsRes = IDR.string.mfid_type_any_document,
        )
        binding.buttonSkip.setOnClickListener { onConfirm() }
        binding.buttonCancel.setOnClickListener { onCancel() }
    }
}
