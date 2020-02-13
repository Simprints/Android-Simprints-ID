package com.simprints.testtools.common.di

import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.spy
import io.mockk.mockk
import io.mockk.spyk

sealed class DependencyRule {
    object RealRule : DependencyRule()
    object MockRule : DependencyRule()
    object MockkRule : DependencyRule() //Required for coroutines
    object SpyRule : DependencyRule()
    object SpykRule : DependencyRule()
    class ReplaceRule<T>(var replacementProvider: () -> T) : DependencyRule()

    inline fun <reified T: Any> resolveDependency(provider: () -> T): T =
        when (this) {
            is RealRule -> provider()
            is MockRule -> mock()
            is MockkRule -> mockk(relaxed = true)
            is SpyRule -> spy(provider())
            is SpykRule -> spyk(provider())
            is ReplaceRule<*> -> this.replacementProvider() as T
        }
}
