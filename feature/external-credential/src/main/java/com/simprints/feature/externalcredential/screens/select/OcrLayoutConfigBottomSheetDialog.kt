package com.simprints.feature.externalcredential.screens.select

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.model.ExternalCredentialResult

class OcrLayoutConfigBottomSheetDialog(
    context: Context,
    private val initialConfig: ExternalCredentialPreviewLayoutConfig,
    private val currentExternalCredentialResult: ExternalCredentialResult,
    private val onDismissed: (String, ExternalCredentialResult) -> Unit = { _, _ -> }
) : BottomSheetDialog(context) {

    private lateinit var v_textSubjectId: TextView
    private lateinit var v_externalCredentialText: TextView
    private lateinit var v_confirmCredentialCheckbox: CheckBox
    private lateinit var v_iconExternalCredential: ImageView
    private lateinit var v_userMessage: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_layout_ocr_config, null)
        setContentView(view)

        setOnDismissListener {
            onDismissed(v_userMessage.text?.toString()?.trim().orEmpty(), currentExternalCredentialResult)
        }

        v_textSubjectId = view.findViewById(R.id.textSubjectId)
        v_externalCredentialText = view.findViewById(R.id.externalCredentialText)
        v_confirmCredentialCheckbox = view.findViewById(R.id.confirmCredentialCheckbox)
        v_iconExternalCredential = view.findViewById(R.id.iconExternalCredential)
        v_userMessage = view.findViewById(R.id.userMessage)

        renderCard(currentExternalCredentialResult, initialConfig)
        val bottomSheet = delegate.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
    }

    private fun renderCard(currentExternalCredentialResult: ExternalCredentialResult, config: ExternalCredentialPreviewLayoutConfig) {
        when (currentExternalCredentialResult) {
            ExternalCredentialResult.ENROL_OK -> renderCardEnrolOk()
            ExternalCredentialResult.ENROL_DUPLICATE_FOUND -> renderCardDuplicateFound(config)
            ExternalCredentialResult.SEARCH_FOUND -> renderCardSearchFound(config)
            ExternalCredentialResult.SEARCH_NOT_FOUND -> renderCardSearchNotFound(config)
            ExternalCredentialResult.CREDENTIAL_EMPTY -> renderCardEmptyCredential(config)
        }
    }

    private fun renderCardEnrolOk() {
        val textColor = ContextCompat.getColor(context, com.simprints.infra.resources.R.color.simprints_text_grey)
        v_textSubjectId.setTextColor(textColor)
        v_confirmCredentialCheckbox.isVisible = true
    }

    private fun renderCardDuplicateFound(config: ExternalCredentialPreviewLayoutConfig) {
        val textColor = ContextCompat.getColor(context, com.simprints.infra.resources.R.color.simprints_red_dark)
        val text = config.userMessages[ExternalCredentialResult.ENROL_DUPLICATE_FOUND] ?: "This credential is already enroled!"
        v_textSubjectId.text = text
        v_textSubjectId.setTextColor(textColor)
        v_iconExternalCredential.setImageResource(R.drawable.ic_warning)
        // [MS-953] For some reason this is the only resolved way to make the icon red
        v_iconExternalCredential.setColorFilter(textColor)
        v_confirmCredentialCheckbox.isVisible = false
        v_userMessage.setText(text)
    }

    private fun renderCardSearchFound(config: ExternalCredentialPreviewLayoutConfig) {
        val textColor = ContextCompat.getColor(context, com.simprints.infra.resources.R.color.simprints_text_grey)
        v_textSubjectId.setTextColor(textColor)
        val text = config.userMessages[ExternalCredentialResult.SEARCH_FOUND] ?: "Subject Found"
        v_textSubjectId.text = text
        v_iconExternalCredential.colorFilter = null
        v_iconExternalCredential.setImageResource(com.simprints.infra.resources.R.drawable.ic_checked_green_large)
        v_confirmCredentialCheckbox.isVisible = false
        v_userMessage.setText(text)
    }

    private fun renderCardSearchNotFound(config: ExternalCredentialPreviewLayoutConfig) {
        val textColor = ContextCompat.getColor(context, com.simprints.infra.resources.R.color.simprints_red_dark)
        val text = config.userMessages[ExternalCredentialResult.SEARCH_NOT_FOUND] ?: "No subject found for this credential"
        v_textSubjectId.text = text
        v_textSubjectId.setTextColor(textColor)
        v_iconExternalCredential.setImageResource(R.drawable.ic_warning)
        // [MS-953] For some reason this is the only resolved way to make the icon red
        v_iconExternalCredential.setColorFilter(textColor)
        v_confirmCredentialCheckbox.isVisible = false
        v_userMessage.setText(text)
    }

    private fun renderCardEmptyCredential(config: ExternalCredentialPreviewLayoutConfig) {
        val textColor = ContextCompat.getColor(context, com.simprints.infra.resources.R.color.simprints_red_dark)
        v_externalCredentialText.text = "???"
        val text = config.userMessages[ExternalCredentialResult.CREDENTIAL_EMPTY] ?: "Cannot read personal identifier\nRescan or enter manually"
        v_textSubjectId.text = text
        v_textSubjectId.setTextColor(textColor)
        v_iconExternalCredential.setImageResource(R.drawable.ic_warning)
        // [MS-953] For some reason this is the only resolved way to make the icon red
        v_iconExternalCredential.setColorFilter(textColor)
        v_confirmCredentialCheckbox.isVisible = false
        v_userMessage.setText(text)
    }
}
