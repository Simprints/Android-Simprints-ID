package com.simprints.infra.logging

@Retention(AnnotationRetention.BINARY)
annotation class ExcludedFromGeneratedTestCoverageReports(
    val reason: String,
)
