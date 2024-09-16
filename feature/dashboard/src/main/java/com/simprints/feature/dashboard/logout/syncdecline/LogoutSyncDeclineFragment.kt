package com.simprints.feature.dashboard.logout.syncdecline

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentLogoutSyncDeclineBinding
import com.simprints.feature.dashboard.logout.LogoutSyncViewModel
import com.simprints.feature.dashboard.settings.password.SettingsPasswordDialogFragment
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class LogoutSyncDeclineFragment : Fragment(R.layout.fragment_logout_sync_decline) {

    private val viewModel by viewModels<LogoutSyncViewModel>()
    private val binding by viewBinding(FragmentLogoutSyncDeclineBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private val confirmationDialogForLogout: AlertDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(IDR.string.dashboard_logout_confirmation_title))
            .setMessage(getString(IDR.string.dashboard_logout_confirmation_message))
            .setPositiveButton(getString(IDR.string.dashboard_logout_confirmation_log_out_button)) { _, _ -> processLogoutConfirmation() }
            .setNegativeButton(
                getString(IDR.string.dashboard_logout_confirmation_cancel_button), null
            ).create()
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
            onSuccess = { processLogoutConfirmation() }
        )

        logoutWithoutSyncConfirmButton.setOnClickListener {
            viewModel.settingsLocked.observe(
                viewLifecycleOwner,
                LiveDataEventWithContentObserver { config ->
                    val password = config.getNullablePassword()
                    if (password != null) {
                        SettingsPasswordDialogFragment.newInstance(
                            title = IDR.string.dashboard_password_lock_title_logout,
                            passwordToMatch = password,
                        ).show(childFragmentManager, SettingsPasswordDialogFragment.TAG)
                    } else {
                        confirmationDialogForLogout.show()
                    }
                })
        }
    }

    private fun processLogoutConfirmation() {
        viewModel.logout()
        findNavController().navigate(R.id.action_logoutSyncDeclineFragment_to_requestLoginFragment)
    }
}
