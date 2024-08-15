package com.simprints.fingerprint.capture.views.confirmfingerprints

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.R
import com.simprints.fingerprint.capture.resources.nameTextId

class ConfirmFingerprintsBottomSheetDialog : BottomSheetDialogFragment() {
    private var scannedFingers: List<Item> = emptyList()
    private var minSuccessfulScans: Int = -1
    private var onPreferableActionClick: () -> Unit = {}
    private var onNotDesirableActionClick: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_confirm_fingerprints, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).also {
            it.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            it.behavior.skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val totalSuccessfulScans = scannedFingers.count { it.numberOfSuccessfulScans > 0 }
            val isEnoughSuccessfulScans = totalSuccessfulScans >= minSuccessfulScans
            val icon = when (isEnoughSuccessfulScans) {
                true -> com.simprints.infra.resources.R.drawable.ic_check_circle_outline_24px
                false -> com.simprints.infra.resources.R.drawable.ic_error_outline_24px
            }
            val notDesirableActionTextRes = when (isEnoughSuccessfulScans) {
                true -> com.simprints.infra.resources.R.string.fingerprint_capture_confirm_fingers_dialog_no
                false -> com.simprints.infra.resources.R.string.fingerprint_capture_confirm_fingers_dialog_yes
            }
            val isContinueButtonVisible = isEnoughSuccessfulScans
            val isRestartButtonVisible = !isEnoughSuccessfulScans

            findViewById<ImageView>(R.id.iconHeader)?.setImageResource(icon)
            findViewById<TextView>(R.id.fingerprintList)?.text = getMapOfFingersAndQualityAsText()
            findViewById<TextView>(R.id.scanResultMessage)?.let {
                it.isVisible = !isEnoughSuccessfulScans
                if (!isEnoughSuccessfulScans) {
                    it.text = getString(
                        com.simprints.infra.resources.R.string.fingerprint_capture_confirm_requirement_text,
                        totalSuccessfulScans,
                        minSuccessfulScans
                    )
                }
            }
            findViewById<Button>(R.id.buttonNotDesirableAction)?.let {
                it.setText(notDesirableActionTextRes)
                it.setOnClickListener { onNotDesirableActionClick() }
            }
            findViewById<View>(R.id.buttonContinue).let {
                it.isVisible = isContinueButtonVisible
                it.setOnClickListener { onPreferableActionClick() }
            }

            findViewById<View>(R.id.buttonRestart)?.let {
                it.isVisible = isRestartButtonVisible
                it.setOnClickListener { onPreferableActionClick() }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getMapOfFingersAndQualityAsText(): String =
        StringBuilder().also {
            scannedFingers.forEach { (fingerName, successes, scans) ->
                if (scans == 1) {
                    it.append(if (successes == 1) "✓ " else "× ")
                } else {
                    it.append("$successes / $scans ")
                }
                it.append(getString(fingerName.nameTextId()) + "\n")
            }
        }.toString()

    data class Item(
        val finger: IFingerIdentifier,
        val numberOfSuccessfulScans: Int,
        val numberOfScans: Int
    )

    companion object {
        fun build(
            scannedFingers: List<Item>,
            minSuccessfulScans: Int,
            onPreferableActionClick: () -> Unit,
            onNotDesirableActionClick: () -> Unit
        ) = ConfirmFingerprintsBottomSheetDialog().also {
            it.scannedFingers = scannedFingers
            it.minSuccessfulScans = minSuccessfulScans
            it.onPreferableActionClick = onPreferableActionClick
            it.onNotDesirableActionClick = onNotDesirableActionClick
        }
    }
}