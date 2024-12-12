package com.simprints.infra.uibase.viewbinding

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding

inline fun <T : ViewBinding> FragmentActivity.viewBinding(crossinline bindingInflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) = FragmentViewBindingDelegate(this, viewBindingFactory)
