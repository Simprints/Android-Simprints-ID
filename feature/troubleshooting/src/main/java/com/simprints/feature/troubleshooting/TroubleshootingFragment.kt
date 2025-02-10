package com.simprints.feature.troubleshooting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.troubleshooting.databinding.FragmentTroubleshootingBinding
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class TroubleshootingFragment : Fragment(R.layout.fragment_troubleshooting) {
    private val binding by viewBinding(FragmentTroubleshootingBinding::bind)

    private val rootViewModel by activityViewModels<TroubleshootingViewModel>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.troubleshootingToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        rootViewModel.shouldOpenIntentDetails.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver<String> {
                findNavController().navigateSafely(
                    this,
                    TroubleshootingFragmentDirections.actionTroubleshootingFragmentToTroubleshootingEventLogFragment(it),
                )
            },
        )

        val adapter = TroubleshootingPagerAdapter(requireActivity())
        binding.troubleshootingPager.adapter = adapter

        TabLayoutMediator(binding.troubleshootingTabs, binding.troubleshootingPager) { tab, position ->
            tab.text = TroubleshootingPagerAdapter.Tabs.entries[position].title
        }.attach()
    }
}
