package com.simprints.id.shared

sealed class DependencyRule {
    class RealRule : DependencyRule()
    class MockRule : DependencyRule()
    class SpyRule : DependencyRule()
    class ReplaceRule<T>(var replacementProvider: () -> T) : DependencyRule()
}
