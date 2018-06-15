package com.simprints.id.shared

sealed class DependencyRule {
    class RealRule : DependencyRule()
    class MockRule : DependencyRule()
    class SpyRule : DependencyRule()
    class ReplaceRule(var replacementProvider: () -> Any) : DependencyRule()
}
