package com.simprints.fingerprint.activities.collect.fingerviewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

class FingerPageAdapter(fragmentActivity: FragmentActivity,
                        private val manager: FingerViewPagerManager) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment = FingerFragment.newInstance(manager.activeFingers[position])
    override fun containsItem(itemId: Long): Boolean = manager.activeFingers.contains(itemId.itemIdToFingerIdentifier())
    override fun getItemCount(): Int = manager.activeFingers.size
    override fun getItemId(position: Int): Long = manager.activeFingers[position].toItemId()

    private fun FingerIdentifier.toItemId(): Long = this.ordinal.toLong()
    private fun Long.itemIdToFingerIdentifier(): FingerIdentifier = FingerIdentifier.values()[this.toInt()]
}
