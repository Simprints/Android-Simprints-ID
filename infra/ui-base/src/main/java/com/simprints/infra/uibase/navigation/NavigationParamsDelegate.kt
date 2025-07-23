package com.simprints.infra.uibase.navigation

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.step.StepParams
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@ExcludedFromGeneratedTestCoverageReports("UI code")
class NavigationParamsDelegate<R : Serializable> : ReadOnlyProperty<Fragment, R> {
    override fun getValue(
        thisRef: Fragment,
        property: KProperty<*>,
    ): R = thisRef.arguments
        ?.getSerializable(PARAM_KEY)
        ?.let { it as? R }
        ?: throw IllegalStateException("Fragment does not define serializable argument")
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun <R : Serializable> navigationParams() = NavigationParamsDelegate<R>()

private const val PARAM_KEY = "param"

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun StepParams?.toBundle() = bundleOf(PARAM_KEY to this)
