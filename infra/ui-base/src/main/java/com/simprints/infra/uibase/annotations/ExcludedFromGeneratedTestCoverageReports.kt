package com.simprints.infra.uibase.annotations

/*
Use this annotation to ignore a class or function from test coverage reports.

Duplicate of annotation in core to avoid depending on core in ui-base module.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.TYPE,
)
annotation class ExcludedFromGeneratedTestCoverageReports(
    val reason: String,
)
