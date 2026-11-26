package com.simprints.feature.troubleshooting

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.troubleshooting.TroubleshootingPagerAdapter.Tabs
import com.simprints.feature.troubleshooting.debug.DebugFragment
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TroubleshootingPagerAdapterTest {
    @Test
    fun `debug build lists the existing tabs in order and ends with the debug tab`() {
        assertThat(BuildConfig.DEBUG).isTrue()

        assertThat(Tabs.visibleEntries())
            .containsExactly(Tabs.Overview, Tabs.Intents, Tabs.Network, Tabs.Workers, Tabs.RealmToRoom, Tabs.Debug)
            .inOrder()
        assertThat(Tabs.visibleEntries().last().title).isEqualTo("DEBUG")
    }

    @Test
    fun `adapter item count, fragments and titles follow the visible tab list`() {
        val adapter = TroubleshootingPagerAdapter(mockk<FragmentManager>(relaxed = true), mockk<Lifecycle>(relaxed = true))
        val visible = Tabs.visibleEntries()

        assertThat(adapter.itemCount).isEqualTo(visible.size)
        assertThat(visible.indices.map { adapter.getTabTitle(it) }).isEqualTo(visible.map { it.title })
        assertThat(adapter.getTabTitle(adapter.itemCount - 1)).isEqualTo("DEBUG")
        assertThat(adapter.createFragment(adapter.itemCount - 1)).isInstanceOf(DebugFragment::class.java)
    }
}
