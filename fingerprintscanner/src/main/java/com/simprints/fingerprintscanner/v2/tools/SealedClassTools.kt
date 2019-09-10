package com.simprints.fingerprintscanner.v2.tools

import kotlin.reflect.KClass

inline fun <reified T: Any> KClass<T>.values() = this.sealedSubclasses.map { it.objectInstance as T }
