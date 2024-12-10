package com.simprints.feature.troubleshooting

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.simprints.feature.troubleshooting.events.EventScopeLogFragment
import com.simprints.feature.troubleshooting.networking.NetworkingLogFragment
import com.simprints.feature.troubleshooting.overview.OverviewFragment
import com.simprints.feature.troubleshooting.workers.WorkerLogFragment
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class TroubleshootingPagerAdapter(
    fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = Tabs.entries.size

    override fun createFragment(position: Int): Fragment = Tabs.entries[position].factory()

    internal enum class Tabs(
        val title: String,
        val factory: () -> Fragment,
    ) {

        Overview("Overview", { OverviewFragment() }),
        Events("Event scopes", { EventScopeLogFragment() }),
        Workers("Worker log", { WorkerLogFragment() }),
        Network("Network log", { NetworkingLogFragment() })
    }
}
