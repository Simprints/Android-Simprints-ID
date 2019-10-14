package com.simprints.fingerprintscanner.v2.tools.lang

import kotlin.reflect.KClass

inline fun <reified T: Any> KClass<T>.objects() = this.sealedSubclasses.mapNotNull { it.objectInstance }
