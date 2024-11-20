package com.simprints.feature.dashboard.settings.troubleshooting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentTroubleshootingBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class TroubleshootingFragment : Fragment(R.layout.fragment_troubleshooting) {

    private val binding by viewBinding(FragmentTroubleshootingBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.troubleshootingToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val adapter = TroubleshootingPagerAdapter(requireActivity())
        binding.troubleshootingPager.adapter = adapter

        TabLayoutMediator(binding.troubleshootingTabs, binding.troubleshootingPager) { tab, position ->
            tab.text = TroubleshootingPagerAdapter.Tabs.entries[position].title
        }.attach()
    }
}
