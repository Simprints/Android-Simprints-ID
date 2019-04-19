package com.simprints.core.tools.json

import androidx.annotation.Keep
import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@Target(AnnotationTarget.PROPERTY)
@Keep
annotation class SkipSerialisationProperty

@Retention(RUNTIME)
@Target(AnnotationTarget.FIELD)
@Keep
annotation class SkipSerialisationField
