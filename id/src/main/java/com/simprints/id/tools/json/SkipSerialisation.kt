package com.simprints.id.tools.json

import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class SkipSerialisationProperty

@Retention(RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class SkipSerialisationField
