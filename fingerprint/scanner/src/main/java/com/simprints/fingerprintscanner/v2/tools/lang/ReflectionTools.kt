package com.simprints.fingerprintscanner.v2.tools.lang

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

inline fun <reified S : Any, reified B : Any> isSubclass(): Boolean = S::class.isSubclassOf(B::class)

inline fun <reified T : Any> KClass<T>.objects() = this.sealedSubclasses.mapNotNull { it.objectInstance }
