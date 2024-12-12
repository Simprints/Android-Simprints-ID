package com.simprints.infra.uibase.viewbinding

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * This class to work as a delegate to view bindings to fragments.
 * for context on this approach see the link below
 * https://zhuinden.medium.com/simple-one-liner-viewbinding-in-fragments-and-activities-with-kotlin-961430c6c07c
 */
class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (View) -> T,
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    private val fragmentObserver = object : DefaultLifecycleObserver {
        val viewLifecycleOwnerLiveDataObserver = Observer<LifecycleOwner?> {
            val viewLifecycleOwner = it ?: return@Observer

            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    binding = null
                }
            })
        }

        override fun onCreate(owner: LifecycleOwner) {
            fragment.viewLifecycleOwnerLiveData.observeForever(viewLifecycleOwnerLiveDataObserver)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            fragment.viewLifecycleOwnerLiveData.removeObserver(viewLifecycleOwnerLiveDataObserver)
        }
    }

    init {
        fragment.lifecycle.addObserver(fragmentObserver)
    }

    override fun getValue(
        thisRef: Fragment,
        property: KProperty<*>,
    ): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        // ensure the fragment's view has been created before creating view-binding
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        // create, assign and return fragment view-binding
        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}
