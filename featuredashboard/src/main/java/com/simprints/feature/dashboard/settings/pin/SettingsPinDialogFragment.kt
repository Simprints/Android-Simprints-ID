package com.simprints.feature.dashboard.settings.pin

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.simprints.feature.dashboard.databinding.FragmentPinInputBinding
import com.google.android.material.R as MR
import com.simprints.infra.resources.R as IDR

class SettingsPinDialogFragment(
    @StringRes val title: Int = IDR.string.pin_lock_title_default,
    val codeToMatch: String,
    val onSuccess: () -> Unit,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog
        .Builder(requireContext(), MR.style.ThemeOverlay_AppCompat_Dialog)
        .setTitle(title)
        .setView(inflateInputView())
        .setNegativeButton(IDR.string.pin_lock_cancel) { _, _ -> dismiss() }
        .create()

    private fun inflateInputView() = FragmentPinInputBinding.inflate(layoutInflater)
        .apply {
            pinInputField.addTextChangedListener(
                afterTextChanged = { text ->
                    resetErrorText(text)
                    checkPin(text)
                }
            )
        }
        .root

    private fun FragmentPinInputBinding.resetErrorText(text: Editable?) {
        if (pinInputLayout.error != null && text?.isNotEmpty() == true) {
            pinInputLayout.error = null
        }
    }

    private fun FragmentPinInputBinding.checkPin(text: Editable?) {
        if (text?.length == codeToMatch.length) {
            if (text.toString() == codeToMatch) {
                onSuccess()
                dismiss()
            } else {
                pinInputField.text = null
                pinInputLayout.error = getString(IDR.string.pin_lock_wrong_pin)
            }
        }
    }

    companion object {
        const val TAG = "SettingsPinDialogFragment"
    }
}
