package com.simprints.feature.dashboard.settings.password

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.simprints.feature.dashboard.databinding.FragmentSettingsPasswordInputBinding
import com.google.android.material.R as MR
import com.simprints.infra.resources.R as IDR

class SettingsPasswordDialogFragment(
    @StringRes val title: Int = IDR.string.password_lock_title_default,
    val codeToMatch: String,
    val onSuccess: () -> Unit,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog
        .Builder(requireContext(), MR.style.ThemeOverlay_AppCompat_Dialog)
        .setTitle(title)
        .setView(inflateInputView())
        .setNegativeButton(IDR.string.password_lock_cancel) { _, _ -> dismiss() }
        .create()

    private fun inflateInputView() = FragmentSettingsPasswordInputBinding.inflate(layoutInflater)
        .apply {
            passwordInputField.addTextChangedListener(
                afterTextChanged = { text ->
                    resetErrorText(text)
                    checkPassword(text)
                }
            )
        }
        .root

    private fun FragmentSettingsPasswordInputBinding.resetErrorText(text: Editable?) {
        if (passwordInputLayout.error != null && text?.isNotEmpty() == true) {
            passwordInputLayout.error = null
        }
    }

    private fun FragmentSettingsPasswordInputBinding.checkPassword(text: Editable?) {
        if (text?.length == codeToMatch.length) {
            if (text.toString() == codeToMatch) {
                onSuccess()
                dismiss()
            } else {
                passwordInputField.text = null
                passwordInputLayout.error = getString(IDR.string.password_lock_wrong_pin)
            }
        }
    }

    companion object {
        const val TAG = "SettingsPasswordDialogFragment"
    }
}
