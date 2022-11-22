package com.simprints.feature.dashboard.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.BuildConfig
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentMainBinding
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class MainFragment : Fragment(R.layout.fragment_main), MenuProvider {

    private val binding by viewBinding(FragmentMainBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dashboardToolbar.title = getString(IDR.string.dashboard_label)
        binding.dashboardToolbar.setOnMenuItemClickListener {
            Simber.i("Click on ${it.itemId}")
            true
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        Simber.i("on prepare")

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.findItem(R.id.debug).isVisible = BuildConfig.DEBUG
        Simber.i("on create")
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        Simber.i("on meny select")
        return true
    }
}
