package com.simprints.feature.dashboard.logout.syncdecline

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentLogoutSyncBinding
import com.simprints.feature.dashboard.databinding.FragmentLogoutSyncDeclineBinding
import com.simprints.feature.dashboard.logout.LogoutSyncViewModel
import com.simprints.feature.dashboard.settings.about.AboutViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogoutSyncDeclineFragment : Fragment(R.layout.fragment_logout_sync_decline) {

    private val viewModel by viewModels<LogoutSyncViewModel>()
    private val binding by viewBinding(FragmentLogoutSyncDeclineBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding) {
        logoutSyncDeclineToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        logoutWithoutSyncCancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
        logoutWithoutSyncConfirmButton.setOnClickListener {
            viewModel.logout()
            //TODO find out what kind of password needs to be entered in order to log out
            findNavController().navigate(R.id.action_logoutSyncDeclineFragment_to_requestLoginFragment)
        }
    }
}
