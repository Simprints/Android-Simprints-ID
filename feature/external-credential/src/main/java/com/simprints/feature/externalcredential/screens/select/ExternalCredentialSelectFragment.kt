package com.simprints.feature.externalcredential.screens.select

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
        initObservers()
        initListeners()
    }

    override fun onDestroyView() {
        dialog?.dismiss()
        dialog = null
        super.onDestroyView()
    }

    private fun initObservers() {
        viewModel.layoutConfigLiveData.observe(viewLifecycleOwner) { config ->
            with(binding) {
                (getView(scanQrTextGrid, scanQrText) as? TextView)?.text = config.uiText.qrText
                (getView(anyDocGrid, anyDoc) as? TextView)?.text = config.uiText.anyDocText
                (getView(ghanaIdTextGrid, ghanaIdText) as? TextView)?.text = config.uiText.ghanaCardText
                (getView(nhisCardTextGrid, nhisCardText) as? TextView)?.text = config.uiText.nhisCardText

                externalCredentialTitle?.text = config.screenTitle

                (getView(topTitleGrid, topTitle) as? TextView)?.text = config.topTitle
                (getView(bottomTitleGrid, bottomTitle) as? TextView)?.text = config.bottomTitle

                getView(topTitleGrid, topTitle)?.isVisible = config.isTopTitleVisible
                getView(bottomTitleGrid, bottomTitle)?.isVisible = config.isBottomTitleVisible
                getView(dividerGrid, divider)?.isVisible = config.isBottomTitleVisible

                layoutOption1?.isVisible = config.layoutStyle == LayoutStyle.Grid
                layoutOption2?.isVisible = config.layoutStyle == LayoutStyle.Vertical
            }
        }
    }

    private fun initListeners() {
        getscanQrCard()?.setOnClickListener {
            findNavController().navigateSafely(
                this,
                ExternalCredentialSelectFragmentDirections.actionExternalCredentialSelectFragmentToExternalCredentialQrScanner(),
            )
        }
        getscanOcrGhanaIdCard()?.setOnClickListener {
            startOcr(OcrDocument.GhanaIdCard)
        }
        getscanOcrGhanaNHISCard()?.setOnClickListener {
            startOcr(OcrDocument.GhanaNHISCard)
        }
        getscanOcrAny()?.setOnClickListener {
            displayExternalCredentialIdDialog()
        }
        binding.skipExternalCredentialScan.setOnClickListener {
            displayExternalCredentialPreviewDialog(
                onConfirm = {
                    dialog?.dismiss()
                    dialog = null
                    viewModel.skipScanning()
                },
                onCancel = {
                    dialog?.dismiss()
                    dialog = null
                }
            )

        }
        binding.externalCredentialTitle?.setOnClickListener {
            openLayoutConfigDialog()
        }

    }
    private fun displayExternalCredentialPreviewDialog(
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        dialog?.dismiss()
        dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_skip_scan_confirm, null)
        val cancelButton = view.findViewById<Button>(R.id.buttonCancel)
        val confirmButton = view.findViewById<Button>(R.id.buttonSkip)

        confirmButton.setOnClickListener { onConfirm() }
        cancelButton.setOnClickListener { onCancel() }

        dialog?.setContentView(view)
        dialog?.setCancelable(false)
        dialog?.show()
        (dialog as? BottomSheetDialog)?.apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }

    }
    private fun getView(gridId: View?, verticalId: View?): View? {
        val currentConfig = viewModel.layoutConfigLiveData.value ?: return null
        return if(currentConfig.layoutStyle == LayoutStyle.Grid) gridId else verticalId
    }

    private fun getscanQrCard(): View? {
        val currentConfig = viewModel.layoutConfigLiveData.value ?: return null
        return if(currentConfig.layoutStyle == LayoutStyle.Grid) binding.scanQrCardGrid else binding.scanQrCard
    }

    private fun getscanOcrGhanaIdCard(): View? {
        val currentConfig = viewModel.layoutConfigLiveData.value ?: return null
        return if(currentConfig.layoutStyle == LayoutStyle.Grid) binding.scanOcrGhanaIdCardGrid else binding.scanOcrGhanaIdCard
    }

    private fun getscanOcrGhanaNHISCard(): View? {
        val currentConfig = viewModel.layoutConfigLiveData.value ?: return null
        return if(currentConfig.layoutStyle == LayoutStyle.Grid) binding.scanOcrGhanaNHISCardGrid else binding.scanOcrGhanaNHISCard
    }

    private fun getscanOcrAny(): View? {
        val currentConfig = viewModel.layoutConfigLiveData.value ?: return null
        return if(currentConfig.layoutStyle == LayoutStyle.Grid) binding.scanOcrAnyGrid else binding.scanOcrAny
    }

    private fun openLayoutConfigDialog() {
        val currentConfig = viewModel.layoutConfigLiveData.value ?: return

        LayoutConfigBottomSheetDialog(
            context = requireContext(),
            initialConfig = currentConfig,
            onDismissed = {
                viewModel.layoutRepository.setConfig(it)
            }
        ).show()
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
        dialog?.dismiss()
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
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        editText.requestFocus()
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
                if (button.isEnabled) {
                    button.performClick()
                }
                true
            } else {
                false
            }
        }
    }
}
