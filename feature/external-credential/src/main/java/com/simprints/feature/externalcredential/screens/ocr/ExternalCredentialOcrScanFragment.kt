package com.simprints.feature.externalcredential.screens.ocr

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.text.Text
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialOcrScanBinding
import com.simprints.feature.externalcredential.model.ExternalCredentialResult
import com.simprints.feature.externalcredential.model.ExternalCredentialValidation
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.ocr.model.OcrScanState
import com.simprints.feature.externalcredential.screens.ocr.view.OcrBlockAdapter
import com.simprints.feature.externalcredential.screens.ocr.view.OcrBlockItem
import com.simprints.feature.externalcredential.screens.ocr.view.ZoomController
import com.simprints.feature.externalcredential.screens.ocr.viewmodel.OcrScanViewModel
import com.simprints.feature.externalcredential.screens.select.OcrLayoutConfig
import com.simprints.feature.externalcredential.screens.select.OcrLayoutConfigBottomSheetDialog
import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class ExternalCredentialOcrScanFragment : Fragment(R.layout.fragment_external_credential_ocr_scan) {
    private val args: ExternalCredentialOcrScanFragmentArgs by navArgs()
    private val flowViewModel: ExternalCredentialViewModel by activityViewModels()
    private val ocrScanViewModel: OcrScanViewModel by viewModels()
    private val binding by viewBinding(FragmentExternalCredentialOcrScanBinding::bind)
    private val zoomController: ZoomController by lazy { ZoomController(binding.imageCardPreview, zoomFactor = 3.0f) }
    private var isEditingCredential = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getOcrImage(args.ocrScanParams.imagePath)?.let { ocrImage ->
            binding.imageCardPreview.setImageBitmap(ocrImage)
            ocrScanViewModel.startOcr(ocrImage, ocrDocument = args.ocrScanParams.ocrParams.ocrDocument)
        }
        initObservers()
        initListeners()
        requireActivity().hideKeyboard()
    }

    private fun getOcrImage(path: String): Bitmap? =
        try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to get [$path] OCR image: ${e.message}", Toast.LENGTH_LONG).show()
            Simber.e("Unable to get [$path] OCR image", e)
            null
        }

    private fun initListeners() = with(binding) {
        buttonConfirm.setOnClickListener {
            flowViewModel.externalCredentialResultDetails.value?.let {
                flowViewModel.confirmAndFinishFlow(credential = it.credential, imagePath = args.ocrScanParams.imagePath)
            }
        }

        buttonRecapture.setOnClickListener {
            flowViewModel.recapture()
        }

        confirmCredentialCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.ocrBoxesOverlay.isVisible = false
            buttonConfirm.isEnabled = isChecked
            if (isChecked) {
                zoomImageAndGlow()
            }
        }

        iconZoom.setOnClickListener {
            binding.ocrBoxesOverlay.isVisible = false
            zoomController.toggleZoom { isZoomed ->
                val iconRes = if (isZoomed) R.drawable.ic_zoom_out else R.drawable.ic_zoom_in
                iconZoom.setImageResource(iconRes)
            }
        }

        buttonShowAllFields.setOnClickListener {
            rvOcrFields.isVisible = true
            buttonShowAllFields.isVisible = false
        }

        iconEditCredential.setOnClickListener {
            processEditCredentialChange()
        }

        externalCredentialEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
                processEditCredentialChange()
                true
            } else {
                false
            }
        }
        scannedIdCard.setOnLongClickListener {
            openLayoutConfigDialog()
            true
        }
    }

    private fun openLayoutConfigDialog() {
        val currentConfig = flowViewModel.ocrLayoutConfigLiveData.value ?: return

        flowViewModel.externalCredentialResultDetails.value?.result?.let {
            OcrLayoutConfigBottomSheetDialog(
                context = requireContext(),
                initialConfig = currentConfig,
                currentExternalCredentialResult = it,
                onDismissed = { message, externalCredentialResult ->
                    flowViewModel.ocrLayoutRepository.updateUserMessage(message, externalCredentialResult)
                }
            ).show()
        }
    }

    private fun initObservers() {
        ocrScanViewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OcrScanState.Finished -> {
                    flowViewModel.validateExternalCredential(credentialId = state.externalCredentialField.second)
                    renderOcrFields(fields = state.fieldIds, ocrAllText = state.ocrAllText)
                }

                OcrScanState.OcrInProgress -> {
                    binding.loadingLayout.isVisible = true
                    binding.contentLayout.isVisible = false
                }

                is OcrScanState.Error -> {
                    binding.externalCredentialText.text = "Error\n${state.message}"
                    binding.loadingLayout.isVisible = false
                    binding.contentLayout.isVisible = true
                }
            }
        }

        flowViewModel.externalCredentialResultDetails.observe(viewLifecycleOwner) { details ->
            renderCard(details, flowViewModel.ocrLayoutRepository.getConfig())
        }

        flowViewModel.ocrLayoutConfigLiveData.observe(viewLifecycleOwner) { config ->
            renderCard(flowViewModel.externalCredentialResultDetails.value, config)
        }
    }

    private fun renderCard(details: ExternalCredentialValidation?, config: OcrLayoutConfig) {
        details?.let {
            val result = details.result
            val credential = details.credential
            binding.buttonConfirm.isEnabled = result != ExternalCredentialResult.ENROL_OK // User needs to click checkbox first
            binding.buttonConfirm.isVisible = listOf(
                ExternalCredentialResult.ENROL_DUPLICATE_FOUND,
                ExternalCredentialResult.CREDENTIAL_EMPTY
            ).none { it == result }
            binding.externalCredentialText.text = credential.data
            binding.textSubjectId.isVisible = result != ExternalCredentialResult.ENROL_OK
            binding.iconExternalCredential.isVisible = result != ExternalCredentialResult.ENROL_OK

            val config = flowViewModel.ocrLayoutRepository.getConfig()
            when (result) {
                ExternalCredentialResult.ENROL_OK -> renderCardEnrolOk(credential)
                ExternalCredentialResult.ENROL_DUPLICATE_FOUND -> renderCardDuplicateFound(config)
                ExternalCredentialResult.SEARCH_FOUND -> renderCardSearchFound(credential, config)
                ExternalCredentialResult.SEARCH_NOT_FOUND -> renderCardSearchNotFound(config)
                ExternalCredentialResult.CREDENTIAL_EMPTY -> renderCardEmptyCredential(config)
            }
            binding.loadingLayout.isVisible = false
            binding.contentLayout.isVisible = true
        }
    }

    @SuppressLint("DefaultLocale")
    private fun renderOcrFields(fields: Map<String, String?>, ocrAllText: Text) {
        val recyclerView: RecyclerView = binding.rvOcrFields
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val header = OcrBlockItem("Requested fields (For Debug)", "")
        val data = fields.map {
            OcrBlockItem(id = it.key, text = it.value ?: "???")
        }
        val headerAllFields = OcrBlockItem("--------------------------------\nALL FIELDS READ WITH OCR", "")
        val allFields = ocrAllText.textBlocks.mapIndexed { index, textBlock ->
            val text = textBlock.lines.joinToString(separator = "\n") { line ->
                "  - ${line.text} (${String.format("%.2f", line.confidence * 100)}%)"
            }
            OcrBlockItem(id = "Block #${index + 1}", text = text, lines = textBlock.lines)
        }
        recyclerView.adapter = OcrBlockAdapter(
            items = listOf(header) + data + listOf(headerAllFields) + allFields,
            onBlockClicked = { ocrBlock ->
                highlightOcrLinesOnImage(ocrBlock.lines)
            },
            onLineClicked = { line -> highlightOcrLinesOnImage(listOf(line)) },
            onUseLineButtonClicked = { line ->
                highlightOcrLinesOnImage(listOf(line))
                binding.externalCredentialText.text = line.text
                flowViewModel.validateExternalCredential(credentialId = binding.externalCredentialText.text.toString())
            },
        )
    }

    private fun highlightOcrLinesOnImage(lines: List<Text.Line>) {
        binding.ocrBoxesOverlay.init(lines, binding.imageCardPreview)
        binding.ocrBoxesOverlay.invalidate()
        binding.ocrBoxesOverlay.isVisible = true
    }

    private fun renderCardEnrolOk(credential: ExternalCredential) {
        val textColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_text_grey)
        binding.textSubjectId.setTextColor(textColor)
        binding.confirmCredentialCheckbox.isVisible = true
        binding.buttonConfirm.setText(IDR.string.face_capture_finish_button)
    }

    private fun renderCardDuplicateFound(config: OcrLayoutConfig) {
        val textColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_red_dark)
        binding.textSubjectId.text = config.userMessages[ExternalCredentialResult.ENROL_DUPLICATE_FOUND] ?: "This credential is already enroled!"
        binding.textSubjectId.setTextColor(textColor)
        binding.iconExternalCredential.setImageResource(R.drawable.ic_warning)
        // [MS-953] For some reason this is the only resolved way to make the icon red
        binding.iconExternalCredential.setColorFilter(textColor)
        binding.confirmCredentialCheckbox.isVisible = false
        binding.buttonConfirm.setText(IDR.string.face_capture_finish_button)
    }

    private fun renderCardSearchFound(credential: ExternalCredential, config: OcrLayoutConfig) {
        val textColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_text_grey)
        binding.textSubjectId.setTextColor(textColor)
        binding.textSubjectId.text = config.userMessages[ExternalCredentialResult.SEARCH_FOUND] ?: "Subject Found"
        binding.iconExternalCredential.colorFilter = null
        binding.iconExternalCredential.setImageResource(IDR.drawable.ic_checked_green_large)
        binding.confirmCredentialCheckbox.isVisible = false
        binding.buttonConfirm.setText(IDR.string.face_capture_finish_button)
    }

    private fun renderCardSearchNotFound(config: OcrLayoutConfig) {
        val textColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_red_dark)
        binding.textSubjectId.text = config.userMessages[ExternalCredentialResult.SEARCH_NOT_FOUND] ?: "No subject found for this credential"
        binding.textSubjectId.setTextColor(textColor)
        binding.iconExternalCredential.setImageResource(R.drawable.ic_warning)
        // [MS-953] For some reason this is the only resolved way to make the icon red
        binding.iconExternalCredential.setColorFilter(textColor)
        binding.confirmCredentialCheckbox.isVisible = false
        binding.buttonConfirm.text = "Search 1:N"
    }

    private fun renderCardEmptyCredential(config: OcrLayoutConfig) {
        val textColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_red_dark)
        binding.externalCredentialText.text = "???"
        binding.textSubjectId.text =
            config.userMessages[ExternalCredentialResult.CREDENTIAL_EMPTY] ?: "Cannot read personal identifier\nRescan or enter manually"
        binding.textSubjectId.setTextColor(textColor)
        binding.iconExternalCredential.setImageResource(R.drawable.ic_warning)
        // [MS-953] For some reason this is the only resolved way to make the icon red
        binding.iconExternalCredential.setColorFilter(textColor)
        binding.confirmCredentialCheckbox.isVisible = false
        binding.buttonConfirm.setText(IDR.string.face_capture_finish_button)
    }

    private fun highlightPreviewWithGlow(durationMs: Long) = with(binding.imageCardGlowOverlay) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(durationMs)
            .withEndAction {
                animate()
                    .alpha(0f)
                    .setDuration(durationMs)
                    .withEndAction {
                        visibility = View.INVISIBLE
                    }
                    .start()
            }
            .start()
    }

    private fun zoomImageAndGlow() {
        if (!zoomController.isZoomed) zoomController.toggleZoom {
            binding.iconZoom.setImageResource(R.drawable.ic_zoom_out)
            // Reaching for attention to confirm the External ID with the visual glow
            highlightPreviewWithGlow(durationMs = 300)
        }
    }

    private fun processEditCredentialChange() = with(binding) {
        isEditingCredential = !isEditingCredential

        if (!isEditingCredential) {
            // Hiding keyboard before making edit text invisible and loosing focus
            requireActivity().hideKeyboard()
        }
        externalCredentialText.isVisible = !isEditingCredential
        externalCredentialEditText.isVisible = isEditingCredential
        buttonConfirm.isEnabled = !isEditingCredential

        if (isEditingCredential) {
            zoomImageAndGlow()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val textColor = ContextCompat.getColor(requireContext(), IDR.color.simprints_blue)
            iconEditCredential.setColorFilter(textColor)
            iconEditCredential.setImageResource(R.drawable.ic_done)
            externalCredentialEditText.setText(externalCredentialText.text)
            externalCredentialEditText.requestFocus()
            externalCredentialEditText.setSelection(externalCredentialEditText.text.length)
            imm.showSoftInput(externalCredentialEditText, InputMethodManager.SHOW_IMPLICIT)
            binding.confirmCredentialCheckbox.isChecked = false
        } else {
            iconEditCredential.colorFilter = null
            iconEditCredential.setImageResource(R.drawable.ic_edit)
            flowViewModel.validateExternalCredential(credentialId = externalCredentialEditText.text.toString())
        }
    }


}
