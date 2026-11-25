package com.simprints.feature.troubleshooting

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.simprints.feature.troubleshooting.debug.DebugFragment
import com.simprints.feature.troubleshooting.intents.IntentLogFragment
import com.simprints.feature.troubleshooting.networking.NetworkingLogFragment
import com.simprints.feature.troubleshooting.overview.OverviewFragment
import com.simprints.feature.troubleshooting.recordsmigration.RealmToRoomRecordsMigrationFragment
import com.simprints.feature.troubleshooting.workers.WorkerLogFragment
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class TroubleshootingPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val tabs = Tabs.visibleEntries()

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment = tabs[position].factory()

    fun getTabTitle(position: Int): String = tabs[position].title

    internal enum class Tabs(
        val title: String,
        val factory: () -> Fragment,
    ) {
        Overview("Overview", { OverviewFragment() }),
        Intents("Intents", { IntentLogFragment() }),
        Network("Network", { NetworkingLogFragment() }),
        Workers("Workers", { WorkerLogFragment() }),
        RealmToRoom("Migration", { RealmToRoomRecordsMigrationFragment() }),
        Debug("DEBUG", { DebugFragment() }),
        ;

        companion object {
            /** Tabs shown in this build: [Debug] only exists in debug builds. */
            fun visibleEntries(): List<Tabs> = entries.filter { it != Debug || BuildConfig.DEBUG }
        }
    }
}
