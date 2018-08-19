package com.simprints.id.testUtils.roboletric


import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.spy
import org.mockito.internal.util.reflection.Fields
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadow.api.Shadow.directlyOn
import org.robolectric.shadows.ShadowViewGroup

//@Implements(ViewPager::class)
//Currently not used by Roboletric. Add @Implementation to use it.
class ShadowViewPager : ShadowViewGroup() {

    @RealObject
    internal var realViewPager: ViewPager? = null

    @Implementation
    fun setAdapter(adapter: PagerAdapter) {
        directlyOn(realViewPager, ViewPager::class.java).adapter = addWorkaround(adapter)
    }

    private fun addWorkaround(adapter: PagerAdapter): PagerAdapter? {
        val spied = spy(adapter)
        val fragmentManager = getFragmentManagerFromAdapter(spied)
        doAnswer { invocation ->
            if (fragmentManager!!.fragments.isEmpty())
                invocation.callRealMethod()
            null
        }.`when`(spied).finishUpdate(any())
        return spied
    }

    private fun getFragmentManagerFromAdapter(adapter: PagerAdapter): FragmentManager? {
        for (instanceField in Fields.allDeclaredFieldsOf(adapter).instanceFields()) {
            val obj = instanceField.read()
            if (obj is FragmentManager) {
                return obj
            }
        }
        return null
    }
}
