package com.simprints.feature.externalcredential.screens.select

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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

class QrLayoutConfigBottomSheetDialog(
    context: Context,
    private val initialConfig: ExternalCredentialPreviewLayoutConfig,
    private val qrData: String?,
    private val currentExternalCredentialResult: ExternalCredentialResult,
    private val onDismissed: (String, ExternalCredentialResult) -> Unit = { _, _ -> }
) : BottomSheetDialog(context) {

    private lateinit var externalCredentialSmallIcon: ImageView
    private lateinit var externalCredentialLargeIcon: ImageView
    private lateinit var externalCredentialStatusTitle: TextView
    private lateinit var externalCredentialBody: TextView
    private lateinit var externalCredentialSubjectId: TextView
    private lateinit var userMessageEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_layout_qr_config, null)
        setContentView(view)

        setOnDismissListener {
            onDismissed(userMessageEditText.text?.toString()?.trim().orEmpty(), currentExternalCredentialResult)
        }

        externalCredentialSmallIcon = view.findViewById(R.id.external_credential_small_icon)
        externalCredentialLargeIcon = view.findViewById(R.id.external_credential_large_icon)
        externalCredentialStatusTitle = view.findViewById(R.id.external_credential_status_title)
        externalCredentialBody = view.findViewById(R.id.external_credential_body)
        externalCredentialSubjectId = view.findViewById(R.id.external_credential_subject_id)
        userMessageEditText = view.findViewById(R.id.userMessage)

        renderCard(currentExternalCredentialResult, initialConfig)
        val bottomSheet = delegate.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
    }

    private fun renderCard(currentExternalCredentialResult: ExternalCredentialResult, config: ExternalCredentialPreviewLayoutConfig) {
        when (currentExternalCredentialResult) {
            ExternalCredentialResult.ENROL_OK -> {
                externalCredentialSmallIcon.isVisible = true
                externalCredentialLargeIcon.setImageResource(R.drawable.onboarding_straight)
                externalCredentialLargeIcon.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        com.simprints.infra.resources.R.color.simprints_black
                    )
                )
                val text = config.userMessages[currentExternalCredentialResult] ?: "QR Code scanned"
                externalCredentialStatusTitle.text = text
                userMessageEditText.setText(text)
            }

            ExternalCredentialResult.ENROL_DUPLICATE_FOUND -> {
                externalCredentialSmallIcon.isVisible = false
                externalCredentialSubjectId.isVisible = false
                externalCredentialLargeIcon.setImageResource(R.drawable.ic_warning)
                // [MS-964] For some reason this is the only resolved way to make the icon red
                externalCredentialLargeIcon.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        com.simprints.infra.resources.R.color.simprints_red_dark
                    )
                )
                val text = config.userMessages[currentExternalCredentialResult] ?: "This QR code belongs to another patient"
                externalCredentialStatusTitle.text = text
                userMessageEditText.setText(text)
            }

            ExternalCredentialResult.SEARCH_FOUND -> {
                externalCredentialSmallIcon.isVisible = true
                externalCredentialLargeIcon.setImageResource(R.drawable.onboarding_straight)
                externalCredentialLargeIcon.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        com.simprints.infra.resources.R.color.simprints_black
                    )
                )
                val text = config.userMessages[currentExternalCredentialResult] ?: "Patient found"
                externalCredentialStatusTitle.text = text
                userMessageEditText.setText(text)
            }

            ExternalCredentialResult.SEARCH_NOT_FOUND -> {
                externalCredentialSmallIcon.isVisible = false
                externalCredentialSubjectId.isVisible = false
                externalCredentialLargeIcon.setImageResource(R.drawable.ic_warning)
                // [MS-964] For some reason this is the only resolved way to make the icon red
                externalCredentialLargeIcon.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        com.simprints.infra.resources.R.color.simprints_red_dark
                    )
                )
                val text = config.userMessages[currentExternalCredentialResult] ?: "No patient linked to QR code"
                externalCredentialStatusTitle.text = text
                userMessageEditText.setText(text)
            }

            ExternalCredentialResult.CREDENTIAL_EMPTY -> {
                externalCredentialSmallIcon.isVisible = false
                externalCredentialSubjectId.isVisible = false
                externalCredentialLargeIcon.setImageResource(R.drawable.ic_warning)
                // [MS-964] For some reason this is the only resolved way to make the icon red
                externalCredentialLargeIcon.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        com.simprints.infra.resources.R.color.simprints_red_dark
                    )
                )
                val text = config.userMessages[currentExternalCredentialResult] ?: "Cannot process QR code data"
                externalCredentialStatusTitle.text = text
                userMessageEditText.setText(text)
            }
        }
        externalCredentialBody.text = "QR data: ${qrData ?: "???"}"
    }

}
