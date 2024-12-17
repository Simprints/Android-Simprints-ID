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
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.R
import com.simprints.fingerprint.capture.resources.nameTextId
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
class ConfirmFingerprintsDialog : BottomSheetDialogFragment() {
    private var state: ConfirmationDialogState? = null

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
            it.behavior.isDraggable = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val state = this.state ?: return
        with(view) {
            val scannedFingers: List<Item> = state.items
            val minSuccessfulScansRequired: Int = state.minSuccessfulScansRequired
            val totalSuccessfulScans = scannedFingers.count { it.numberOfSuccessfulScans > 0 }
            val isEnoughSuccessfulScans = when (state) {
                is ConfirmationDialogState.EnoughSuccessfulScans -> true
                is ConfirmationDialogState.NotEnoughSuccessfulScans -> false
            }
            val icon = when (isEnoughSuccessfulScans) {
                true -> IDR.drawable.ic_check_circle_outline_24px
                false -> IDR.drawable.ic_error_outline_24px
            }
            val notDesirableActionTextRes = when (isEnoughSuccessfulScans) {
                true -> IDR.string.fingerprint_capture_confirm_fingers_dialog_no
                false -> IDR.string.fingerprint_capture_confirm_fingers_dialog_yes
            }
            val isContinueButtonVisible = isEnoughSuccessfulScans
            val isRestartButtonVisible = !isEnoughSuccessfulScans

            findViewById<ImageView>(R.id.iconHeader)?.setImageResource(icon)
            findViewById<TextView>(R.id.fingerprintList)?.text =
                getMapOfFingersAndQualityAsText(state)
            findViewById<TextView>(R.id.scanResultMessage)?.let {
                it.isVisible = !isEnoughSuccessfulScans
                if (!isEnoughSuccessfulScans) {
                    it.text = getString(
                        IDR.string.fingerprint_capture_confirm_requirement_text,
                        totalSuccessfulScans,
                        minSuccessfulScansRequired
                    )
                }
            }
            findViewById<Button>(R.id.buttonNotDesirableAction)?.let {
                it.setText(notDesirableActionTextRes)
                it.setOnClickListener {
                    when (state) {
                        is ConfirmationDialogState.EnoughSuccessfulScans -> state.onRestartClick()
                        is ConfirmationDialogState.NotEnoughSuccessfulScans -> state.onContinueClick()
                    }
                }
            }
            findViewById<View>(R.id.buttonContinue).let {
                it.isVisible = isContinueButtonVisible
                it.setOnClickListener {
                    state.onContinueClick()
                }
            }

            findViewById<View>(R.id.buttonRestart)?.let {
                it.isVisible = isRestartButtonVisible
                it.setOnClickListener { state.onRestartClick() }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getMapOfFingersAndQualityAsText(state: ConfirmationDialogState): String =
        StringBuilder().also {
            state.items.forEachIndexed { index, item ->
                val fingerName = item.finger
                val successes = item.numberOfSuccessfulScans
                val scans = item.numberOfScans
                if (scans == 1) {
                    it.append(if (successes == 1) "✓ " else "× ")
                } else {
                    it.append("$successes / $scans ")
                }
                it.append(getString(fingerName.nameTextId()))
                if (index < state.items.size - 1) {
                    it.append("\n")
                }
            }
        }.toString()

    sealed class ConfirmationDialogState {
        abstract val items: List<Item>
        abstract val minSuccessfulScansRequired: Int
        abstract val onContinueClick: () -> Unit
        abstract val onRestartClick: () -> Unit

        data class EnoughSuccessfulScans(
            override val items: List<Item>,
            override val minSuccessfulScansRequired: Int,
            override val onContinueClick: () -> Unit,
            override val onRestartClick: () -> Unit
        ) : ConfirmationDialogState()

        data class NotEnoughSuccessfulScans(
            override val items: List<Item>,
            override val minSuccessfulScansRequired: Int,
            override val onContinueClick: () -> Unit,
            override val onRestartClick: () -> Unit
        ) : ConfirmationDialogState()

    }

    data class Item(
        val finger: IFingerIdentifier,
        val numberOfSuccessfulScans: Int,
        val numberOfScans: Int
    )

    companion object {
        const val TAG = "ConfirmFingerprintsDialog"
        fun build(state: ConfirmationDialogState) =
            ConfirmFingerprintsDialog().also { it.state = state }
    }
}
