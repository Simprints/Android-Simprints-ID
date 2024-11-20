package com.simprints.feature.dashboard.settings.troubleshooting

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
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

        // TODO Replace stub fragments with proper ones
        Overview("Overview", { StubFragment() }),
        Network("Network", { StubFragment() }),
        Events("Events", { StubFragment() }),
        Workers("Worker log", { StubFragment() }),
        ;
    }
}
