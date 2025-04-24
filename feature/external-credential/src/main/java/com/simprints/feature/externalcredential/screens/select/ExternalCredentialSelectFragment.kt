package com.simprints.feature.externalcredential.screens.select

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialSelectBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.ocr.model.OcrDocument
import com.simprints.feature.externalcredential.screens.ocr.model.OcrId
import com.simprints.feature.externalcredential.screens.ocr.model.OcrParams
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ExternalCredentialSelectFragment : Fragment(R.layout.fragment_external_credential_select) {
    private val viewModel: ExternalCredentialViewModel by activityViewModels()
    private val binding by viewBinding(FragmentExternalCredentialSelectBinding::bind)
    private var dialog: Dialog? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onDestroyView() {
        dialog?.dismiss()
        dialog = null
        super.onDestroyView()
    }

    private fun initListeners() {
        binding.scanQrCard.setOnClickListener {
            findNavController().navigateSafely(
                this,
                ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialQrScanner(),
            )
        }
        binding.scanOcrGhanaIdCard.setOnClickListener {
            startOcr(OcrDocument.GhanaIdCard)
        }
        binding.scanOcrGhanaNHISCard.setOnClickListener {
            startOcr(OcrDocument.GhanaNHISCard)
        }
        binding.scanOcrAny.setOnClickListener {
            displayExternalCredentialIdDialog()
        }
        binding.skipExternalCredentialScan.setOnClickListener {
            viewModel.skipScanning()
        }

    }

    private fun startOcr(ocrDocument: OcrDocument) {
        findNavController().navigateSafely(
            this,
            ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialOcrPreview(
                ocrParams = OcrParams(
                    ocrDocument = ocrDocument
                )
            ),
        )
    }


    private fun displayExternalCredentialIdDialog() {
        dialog = BottomSheetDialog(requireContext())
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = layoutInflater.inflate(R.layout.dialog_enter_id, null)
        val button = view.findViewById<View>(R.id.buttonScan)
        val editText = view.findViewById<EditText>(R.id.ocrDocumentId)
        editText.addTextChangedListener {
            button.isEnabled = (it?.length ?: 0) >= 2
        }
        view.findViewById<View>(R.id.buttonScan).setOnClickListener {
            dialog?.dismiss()
            editText.text?.toString()?.let { fieldId ->
                startOcr(
                    ocrDocument = OcrDocument.Custom(
                        listOf(OcrId.FuzzySearch(name = fieldId, fieldOnTheDocument = fieldId, isExternalCredentialId = true))
                    )
                )
            }
        }
        dialog?.setContentView(view)
        dialog?.show()
        editText.requestFocus()
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
                button.performClick()
                true
            } else {
                false
            }
        }
    }
}
