package com.simprints.fingerprint.capture.views.fingerviewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class FingerPageAdapter(
    parent: Fragment,
    private val activeFingers: MutableList<IFingerIdentifier>,
) : FragmentStateAdapter(parent) {
    override fun createFragment(position: Int): Fragment = FingerFragment.newInstance(activeFingers[position])

    override fun containsItem(itemId: Long): Boolean = activeFingers.contains(itemId.itemIdToFingerIdentifier())

    override fun getItemCount(): Int = activeFingers.size

    override fun getItemId(position: Int): Long = activeFingers[position].toItemId()

    private fun IFingerIdentifier.toItemId(): Long = this.ordinal.toLong()

    private fun Long.itemIdToFingerIdentifier(): IFingerIdentifier = IFingerIdentifier.values()[this.toInt()]
}
