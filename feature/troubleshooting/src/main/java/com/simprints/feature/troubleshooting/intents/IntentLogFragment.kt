package com.simprints.feature.troubleshooting.intents

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.TroubleshootingViewModel
import com.simprints.feature.troubleshooting.adapter.TroubleshootingListAdapter
import com.simprints.feature.troubleshooting.databinding.FragmentTroubleshootingListBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
internal class IntentLogFragment : Fragment(R.layout.fragment_troubleshooting_list) {
    private val binding by viewBinding(FragmentTroubleshootingListBinding::bind)
    private val viewModel by viewModels<IntentLogViewModel>()
    private val rootViewModel by activityViewModels<TroubleshootingViewModel>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.logs.observe(viewLifecycleOwner) {
            binding.troubleshootingListProgress.isGone = it.isNotEmpty()
            binding.troubleshootingList.adapter = TroubleshootingListAdapter(it) {
                rootViewModel.openIntentDetails(it)
            }
        }
        viewModel.collectData()
    }
}
