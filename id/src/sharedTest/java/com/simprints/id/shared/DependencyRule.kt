package com.simprints.id.shared

import com.simprints.testframework.common.syntax.mock
import org.mockito.Mockito.spy

sealed class DependencyRule {
    object RealRule : DependencyRule()
    object MockRule : DependencyRule()
    object SpyRule : DependencyRule()
    class ReplaceRule<T>(var replacementProvider: () -> T) : DependencyRule()

    inline fun <reified T> resolveDependency(provider: () -> T): T =
        when (this) {
            is RealRule -> provider()
            is MockRule -> mock()
            is SpyRule -> spy(provider())
            is ReplaceRule<*> -> this.replacementProvider() as T
        }
}
