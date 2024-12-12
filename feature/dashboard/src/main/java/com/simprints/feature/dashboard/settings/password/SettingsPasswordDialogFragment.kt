package com.simprints.feature.dashboard.settings.password

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.feature.dashboard.databinding.FragmentSettingsPasswordInputBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class SettingsPasswordDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(arguments?.getInt(ARG_TITLE) ?: IDR.string.dashboard_password_lock_title_default)
        .setView(inflateInputView())
        .setNegativeButton(IDR.string.dashboard_password_lock_cancel) { _, _ -> dismiss() }
        .create()

    private fun inflateInputView() = FragmentSettingsPasswordInputBinding
        .inflate(layoutInflater)
        .apply {
            passwordInputField.addTextChangedListener(
                afterTextChanged = { text ->
                    resetErrorText(text)
                    checkPassword(text)
                },
            )
        }.root

    private fun FragmentSettingsPasswordInputBinding.resetErrorText(text: Editable?) {
        if (passwordInputLayout.error != null && text?.isNotEmpty() == true) {
            passwordInputLayout.error = null
        }
    }

    private fun FragmentSettingsPasswordInputBinding.checkPassword(text: Editable?) {
        val passwordToMatch = arguments?.getString(ARG_PASSWORD).orEmpty()
        if (text?.length == passwordToMatch.length) {
            if (text.toString() == passwordToMatch) {
                parentFragmentManager.setFragmentResult(
                    RESULT_KEY,
                    bundleOf(
                        RESULT_SUCCESS to true,
                        // Pass-through the action argument to resolve action on call side
                        ARG_ACTION to arguments?.getString(ARG_ACTION),
                    ),
                )
                dismiss()
            } else {
                passwordInputField.text = null
                passwordInputLayout.error = getString(IDR.string.dashboard_password_lock_wrong_pin)
            }
        }
    }

    companion object {
        const val TAG = "SettingsPasswordDialogFragment"

        private const val ARG_TITLE = "titleRes"
        private const val ARG_PASSWORD = "toMatch"
        private const val ARG_ACTION = "action"

        private const val RESULT_KEY = "$TAG-result"
        private const val RESULT_SUCCESS = "success"

        fun newInstance(
            passwordToMatch: String,
            title: Int = IDR.string.dashboard_password_lock_title_default,
            action: String? = null,
        ): DialogFragment = SettingsPasswordDialogFragment().also {
            it.arguments = bundleOf(
                ARG_TITLE to title,
                ARG_ACTION to action,
                ARG_PASSWORD to passwordToMatch,
            )
        }

        fun registerForResult(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onSuccess: (String?) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(RESULT_KEY, lifecycleOwner) { key, result ->
                if (result.getBoolean(RESULT_SUCCESS)) onSuccess(result.getString(ARG_ACTION))
            }
        }
    }
}
