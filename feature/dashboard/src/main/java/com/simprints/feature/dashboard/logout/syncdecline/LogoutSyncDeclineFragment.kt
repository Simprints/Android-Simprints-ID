package com.simprints.feature.dashboard.logout.syncdecline

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentLogoutSyncDeclineBinding
import com.simprints.feature.dashboard.view.StateDrivenDialog
import com.simprints.infra.uibase.password.SettingsPasswordDialogFragment
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class LogoutSyncDeclineFragment : Fragment(R.layout.fragment_logout_sync_decline) {
    private val viewModel by viewModels<LogoutSyncDeclineViewModel>()
    private val binding by viewBinding(FragmentLogoutSyncDeclineBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeState()
    }

    private val logoutConfirmationDialog = StateDrivenDialog(
        onCancelledByUser = { viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmationDismissed) },
    )

    override fun onDestroyView() {
        logoutConfirmationDialog.release()
        super.onDestroyView()
    }

    private fun initViews() = with(binding) {
        logoutSyncDeclineToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        logoutWithoutSyncCancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
        SettingsPasswordDialogFragment.registerForResult(
            fragmentManager = childFragmentManager,
            lifecycleOwner = this@LogoutSyncDeclineFragment,
            onSuccess = { processLogoutConfirmation() },
        )

        logoutWithoutSyncConfirmButton.setOnClickListener {
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    private fun render(state: LogoutSyncDeclineUiState) {
        logoutConfirmationDialog.render(state.isLogoutConfirmationVisible, ::createLogoutConfirmationDialog)
    }

    private fun createLogoutConfirmationDialog() = MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(IDR.string.dashboard_logout_confirmation_title))
        .setMessage(getString(IDR.string.dashboard_logout_confirmation_message))
        .setPositiveButton(getString(IDR.string.dashboard_logout_confirmation_log_out_button)) { _, _ -> processLogoutConfirmation() }
        .setNegativeButton(getString(IDR.string.dashboard_logout_confirmation_cancel_button)) { dialog, _ -> dialog.cancel() }
        .create()

    private fun handleEffect(effect: LogoutSyncDeclineUiEffect) {
        when (effect) {
            is LogoutSyncDeclineUiEffect.ShowPasswordDialog -> SettingsPasswordDialogFragment
                .newInstance(
                    title = IDR.string.dashboard_password_lock_title_logout,
                    passwordToMatch = effect.password,
                ).show(childFragmentManager, SettingsPasswordDialogFragment.TAG)
        }
    }

    private fun processLogoutConfirmation() {
        viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed)
    }
}
