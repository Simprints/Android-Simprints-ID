package com.simprints.fingerprint.capture.views.confirmfingerprints

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.simprints.infra.resources.R as IDR
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simprints.fingerprint.capture.R

class ConfirmActionDialog : BottomSheetDialogFragment() {

    private var state: ConfirmActionDialogState? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_confirm_action, container, false)
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
            val (titleTextRes, bodyTextRes, buttonTextRes) = when (state) {
                is ConfirmActionDialogState.Continue -> {
                    Triple(
                        IDR.string.fingerprint_capture_confirm_action_continue_title,
                        IDR.string.fingerprint_capture_confirm_action_continue_body,
                        IDR.string.fingerprint_capture_confirm_action_continue_button,
                    )
                }

                is ConfirmActionDialogState.Restart -> {
                    Triple(
                        IDR.string.fingerprint_capture_confirm_action_restart_title,
                        IDR.string.fingerprint_capture_confirm_action_restart_body,
                        IDR.string.fingerprint_capture_confirm_action_restart_button,
                    )
                }
            }

            findViewById<TextView>(R.id.confirmActionTitle)?.setText(titleTextRes)
            findViewById<TextView>(R.id.confirmActionBody)?.setText(bodyTextRes)
            findViewById<Button>(R.id.buttonNotDesirableAction)?.let {
                it.setText(buttonTextRes)
                it.setOnClickListener { state.onConfirmActionClick() }
            }
            findViewById<Button>(R.id.buttonCancel)?.setOnClickListener {
                state.onCancelClick()
            }
        }
    }

    sealed class ConfirmActionDialogState {
        abstract val onCancelClick: () -> Unit
        abstract val onConfirmActionClick: () -> Unit

        data class Continue(
            override val onCancelClick: () -> Unit,
            override val onConfirmActionClick: () -> Unit
        ) : ConfirmActionDialogState()

        data class Restart(
            override val onCancelClick: () -> Unit,
            override val onConfirmActionClick: () -> Unit
        ) : ConfirmActionDialogState()
    }

    companion object {
        const val TAG = "ConfirmActionDialog"
        fun build(state: ConfirmActionDialogState): ConfirmActionDialog =
            ConfirmActionDialog().also { it.state = state }
    }

}
