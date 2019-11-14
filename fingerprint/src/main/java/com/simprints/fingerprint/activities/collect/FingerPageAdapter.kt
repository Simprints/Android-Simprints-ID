package com.simprints.fingerprint.activities.collect

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.simprints.fingerprint.activities.collect.models.Finger
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper


class FingerPageAdapter(fragmentManager: FragmentManager,
                        private val activeFingers: List<Finger>,
                        private val androidResourcesHelper: FingerprintAndroidResourcesHelper) :
    FragmentStatePagerAdapter(fragmentManager) {

    private val fragmentSparseArray = SparseArray<FingerFragment>()

    override fun getItem(pos: Int) = FingerFragment.newInstance(activeFingers[pos], androidResourcesHelper).also {
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
