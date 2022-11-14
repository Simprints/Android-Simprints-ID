package com.simprints.feature.dashboard.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentMainBinding
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding(FragmentMainBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dashboardToolbar.setOnMenuItemClickListener {
            Simber.i("Click on ${it.itemId}")
            true
        }
    }
}
