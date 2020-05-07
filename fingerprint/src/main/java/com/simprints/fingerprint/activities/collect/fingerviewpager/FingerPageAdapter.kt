package com.simprints.fingerprint.activities.collect.fingerviewpager

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.simprints.fingerprint.activities.collect.domain.Finger

class FingerPageAdapter(fragmentManager: FragmentManager,
                        private val activeFingers: List<Finger>) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentSparseArray = SparseArray<FingerFragment>()

    override fun getItem(pos: Int) = FingerFragment.newInstance(
        activeFingers[pos]
    ).also {
        fragmentSparseArray.append(pos, it)
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        super.destroyItem(container, position, item)
        fragmentSparseArray.remove(position)
    }

    fun getFragment(pos: Int): FingerFragment? = fragmentSparseArray.get(pos)

    override fun getCount() = activeFingers.size

    override fun getItemPosition(item: Any) = PagerAdapter.POSITION_NONE
}
