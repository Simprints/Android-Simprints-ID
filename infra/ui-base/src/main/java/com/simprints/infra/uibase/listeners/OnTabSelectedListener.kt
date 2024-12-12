package com.simprints.infra.uibase.listeners

import com.google.android.material.tabs.TabLayout

class OnTabSelectedListener(
    private val onSelected: (tab: TabLayout.Tab) -> Unit,
) : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab) {
        onSelected(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // No-op
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        // No-op
    }
}
